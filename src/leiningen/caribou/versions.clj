(ns leiningen.caribou.versions
  (:require [clojure.string :as string]
            [clojure.data.json :as json]))

(defn- maybe-load
  [file]
  (try (load-file file)
       ;; for file not found / not parsable, return an empty map because
       ;; we expect a single defproject form and that returns a map
       (catch Exception e {})))

(defmacro defproject
  "to get a map by loading a project.clj"
  [name v-string & args]
  `(apply hash-map (concat [:name '~name :version ~v-string] '~args)))

(defn current-version
  [map symbol]
  (let [spec (filter #(= (first %) symbol) (:dependencies map))]
    (second (first spec))))

(defn deps
  [file core-v admin-v api-v frontend-v]
  (let [f (maybe-load file)
        core (current-version f 'antler/caribou-core)
        admin (current-version f 'antler/caribou-admin)
        api (current-version f 'antler/caribou-api)
        frontend (current-version f 'antler/caribou-frontend)]
    (filter (!= (% 1) (% 2))
            [[core-v core "core"]
             [admin-v admin "admin"]
             [api-v api "api"]
             [frontend-v frontend "frontend"]])))

(defn parse-version
  [v]
  (map (Integer. %)
       (string/split v #"\.")))

(defn compare-versions
  [a b]
  (cond (and (empty? a) (empty? b)) 0
        (empty? a) 1 ; sorting in reverse order so largest is at head
        (empty? b) -1
        (= (first a) (first b)) (compare-versions (rest a)
                                                  (rest b))
        :default (* -1 (compare (first a) (first b)))))

(defn get-version
  [name]
  (let [url (str "https://clojars.org/search?q="
                 name "&format=json")
        json-string (slurp url)
        json (json/read-json json-string true)
        versions (map :version results)
        sortable-versions (map parse-version versions)
        sorted-versions (sort compare-versions sortable-versions)
        greatest (first sorted-versions)
        version-string (string/join "." greatest)]
    version-string))

(defn versions
  [& args]
  (let [core-v (get-version "caribou-core")
        admin-v (get-version "caribou-admin")
        api-v (get-version "caribou-api")
        frontend-v (get-version "caribou-frontend")
        base (deps "project.clj" core-v admin-v api-v frontend-v)
        admin (deps "admin/project.clj" core-v admin-v api-v frontend-v)
        api (deps "api/project.clj" core-v admin-v api-v frontend-v)
        site (deps "site/project.clj" core-v admin-v api-v frontend-v)]
    (doseq (fn [name versions]
             (println "\nin " name ":")
             (if (empty? versions)
               (println "all dependencies up to date")
               (doseq (v versions)
                 (println (v 2) " is at " (v 1) " upstream is " (v 0)))))
      ["./project.clj" "admin/project.clj" "api/project.clj" "site/project.clj"]
      [base admin api site])))