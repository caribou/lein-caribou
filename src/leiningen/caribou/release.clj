(ns leiningen.caribou.release
  (:require [clojure.string :as string]
            [clojure.java.shell :as sh]
            [clojure.pprint :as pprint]))

(def version-increments (atom {}))

(defn read-project
  [path]
  (let [raw-project (slurp path)
        full-project (read-string raw-project)
        project (into {} (map vec (partition 2 (drop 3 full-project))))
        project-name (nth full-project 1)
        version (nth full-project 2)]
    (assoc project
      :name project-name
      :version version)))

(defn write-project
  [project path]
  (let [project-name (:name project)
        version (:version project)
        values (dissoc project :name :version)
        cat (mapcat identity (seq values))
        output (concat (list 'defproject project-name version) cat)
        raw (with-out-str 
              (binding [pprint/*print-miser-width* 200] 
                (pprint/pprint output)))]
    (println path) 
    (println raw)))
    ;; (spit path raw)))

(defn increment-version-number
  [version]
  (let [[major minor patch] (string/split version #"\.")
        patch-number (Integer/parseInt patch)]
    (str major "." minor "." (inc patch-number))))

(defn update-project-version
  [project version]
  (assoc project :version version))

(defn increment-project-version
  [project]
  (let [project-name (:name project)
        previous (get @version-increments project-name)]
    (if previous
      (assoc project :version previous)
      (let [version (increment-version-number (:version project))]
        (swap! version-increments assoc project-name version)
        (assoc project :version version)))))

(defn update-dependency-version
  [project dependency version]
  (let [dep-symbol (symbol dependency)]
    (update-in
     project
     [:dependencies]
     #(map
       (fn [dep]
         (if (= dep-symbol (first dep))
           (assoc dep 1 version)
           dep))
       %))))

(def dependency-tree
  {'antler/caribou-core ['antler/caribou-frontend
                         'antler/lein-caribou]
   'antler/caribou-frontend ['antler/caribou-api
                             'antler/caribou-admin
                             'antler/caribou-development]
   'antler/caribou-api ['antler/caribou-development]
   'antler/caribou-admin ['antler/caribou-development]
   'antler/caribou-development []
   'antler/antlers ['antler/caribou-frontend]
   'antler/lein-caribou []})

(defn build-project-path
  [project]
  (str "../" project "/project.clj"))

(defn propagate-dependencies
  [dep-symbol version]
  (let [deps (get dependency-tree dep-symbol)]
    (doseq [sub-symbol deps]
      (println "propagating" sub-symbol)
      (let [[package project-name] (string/split (str sub-symbol) #"/")
            path (build-project-path project-name)
            project (read-project path)
            project (increment-project-version project)
            updated (update-dependency-version project dep-symbol version)
            sub-version (:version updated)]
        (if-let [sub-projects (:sub project)]
          (doseq [sub-dir sub-projects]
            (println "propagating" sub-dir)
            (let [sub-path (build-project-path (str project-name "/" sub-dir))
                  sub-project (read-project sub-path)
                  sub-project (increment-project-version sub-project)
                  sub-updated (update-dependency-version sub-project dep-symbol version)]
              (write-project sub-updated sub-path))))
        (write-project updated path)
        (propagate-dependencies sub-symbol sub-version)))))

(defn git-commit-tag-push
  [])

(defn push-to-clojars
  [])

(defn release-project
  [project-name version]
  (let [project-path (build-project-path project-name)
        project (read-project project-path)
        project-symbol (:name project)
        updated (update-project-version project version)]
    (write-project updated project-path)
    (swap! version-increments assoc project-symbol version)
    (propagate-dependencies project-symbol version)
    (git-commit-tag-push)
    (push-to-clojars)))

(defn release
  ([project]
     (println "PROJECT" project)
     (release project (increment-version-number (:version project))))
  ([project version]
     (release-project (:name project) version)))
