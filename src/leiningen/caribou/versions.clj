(ns leiningen.caribou.versions
  (:require [leiningen.core.project :as project]
            [clojure.string :as string]
            [clojure.data.json :as json]))

(defn- dbg
  [& args]
  (let [end (last args)]
    (doseq [a args] (println a))
    end))
    

(defn maybe-load
  [file]
  (try (project/read file)
       ;; for file not found / not parsable, return an empty map because
       ;; we expect a single defproject form and that returns a map
       (catch Exception e (do (prn e) {}))))

(defmacro defproject
  "to get a map by loading a project.clj"
  [name v-string & args]
  `(apply hash-map (concat [:name '~name :version ~v-string] '~args)))

(defn current-version
  [map symbol]
  (let [spec (filter #(= (first %) symbol) (:dependencies map))]
    (second (first spec))))

(defn !=
  [& args]
  (not (apply = args)))

(defn deps
  [core-v admin-v api-v frontend-v file]
 (let [f (maybe-load file)
       core (current-version f 'antler/caribou-core)
       admin (current-version f 'antler/caribou-admin)
       api (current-version f 'antler/caribou-api)
       frontend (current-version f 'antler/caribou-frontend)]
;   (println "f is " f)
   (doall (filter #(and (% 0) (% 1))
                  [[core-v core "core"]
                   [admin-v admin "admin"]
                   [api-v api "api"]
                   [frontend-v frontend "frontend"]]))))

(defn parse-version
  [v]
  (map #(if (re-find #"[^0-9]" %)
          %
         (Integer. %))
       (string/split v #"\.")))

(defn compare-versions
  [a b]
  (cond
    (and (empty? a) (empty? b)) 0
    (empty? a) 1 ; sorting in reverse order so largest is at head
    (empty? b) -1
    (= (first a) (first b)) (compare-versions (rest a)
                                              (rest b))
    (or (not (number? (first a)))
        (not (number? (first b)))) (* -1 (compare (str (first a))
                                                  (str (first b))))
    :default (* -1 (compare (first a) (first b)))))

(defn get-version
  [name]
  (let [url (str "https://clojars.org/search?q="
                 name "&format=json")
        json-string (slurp url)
        json-obj (json/read-json json-string true)
        versions (map :version (:results json-obj))]
    (if-let [versions (filter identity versions)]
      (let [sortable-versions (map parse-version versions)
            sorted-versions (sort compare-versions sortable-versions)
            greatest (first sorted-versions)
            version-string (string/join "." greatest)]
        version-string))))

(defn versions
  [& args]
  (let [arg (last args)
        path (if (string? arg) arg ".")
        core-v (get-version "caribou-core")
        admin-v (get-version "caribou-admin")
        api-v (get-version "caribou-api")
        frontend-v (get-version "caribou-frontend")
        check-deps (partial deps core-v admin-v api-v frontend-v)
        base-file (str path "/project.clj")
        base (check-deps base-file)
        admin-file (str path "/admin/project.clj")
        admin (check-deps admin-file)
        api-file (str path "/api/project.clj")
        api (check-deps api-file)
        site-file (str path "/site/project.clj")
        site (check-deps site-file)]
    (doseq [check [[base-file base] [admin-file admin]
                   [api-file api] [site-file site]]]
      (println "\nin " (check 0) ":")
      (if (empty? (check 1))
        (println "no monitored dependencies to report")
        (doseq [v (check 1)]
          (println (v 2) " is at " (v 1) " upstream is " (v 0)))))))
