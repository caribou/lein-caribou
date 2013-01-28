;;Provision new caribou project and bootstrap it.

(ns leiningen.caribou.create
  (:require [clojure.string :as string]
            [caribou.model :as model]
            [caribou.db :as db]
            [caribou.config :as config]
            [caribou.tasks.bootstrap :as bootstrap]
            [clojure.java.jdbc :as sql]
            [clojure.pprint :as pprint]
            [leiningen.caribou.migrate :as migrate]
            [leiningen.core.project :as project])
  (:use clojure.java.io
        [zippix.core :only (pathify unzip-resource)]))

(declare ^:dynamic *project* ^:dynamic *project-dir* ^:dynamic *dirs* ^:dynamic *home-dir*)

(defn clean-proj-name
  [n]
  (string/replace n #"-" "_"))

(defn get-file
  [n]
  (slurp (pathify n)))

(defn substitution-map
  []
  [[#"\$project\$" *project*]
   [#"\$project-dir\$" *project-dir*]
   [#"\$safeproject\$" (clean-proj-name *project*)]])

(defn substitute-strings
  [tmpl]
  (reduce 
    (fn [t pair] (string/replace t (first pair) (last pair))) tmpl (substitution-map)))

(defn get-template
  [n]
  (substitute-strings (get-file [(resource n)])))

(defn create-config
  []
  (.mkdirs (file *home-dir* "config"))
  (spit (apply file *home-dir* ["config" "database.yml"]) (get-template "database_caribou.yml")))

(defn find-db-config
  [project-name]
  (config/assoc-subname
   (dissoc
    (assoc @config/db :database (str project-name "_development"))
    :subname)))

;; TODO:kd - this should be a migration:
; (defn create-default
;   []
;   (model/invoke-models)
;   (model/create :page {:name "Home" :path "" :controller "home" :action "home" :template "home.html"}))

(defn tailor-proj
  [dir]
  (let [old-dir (file (str (file dir) "/site/src/skel/"))
        new-dir (file (str (file dir) "/site/src/" (clean-proj-name *project*)))
        old-src-dir (file (str (file dir) "/src/skel"))
        new-src-dir (file (str (file dir) "/src/" (clean-proj-name *project*)))]
    (.renameTo old-dir new-dir)
    (.renameTo old-src-dir new-src-dir))
  (let [files (file-seq (file dir))]
    (doseq [f files]
      (if (.isFile f)
        (let [content (slurp (str f))]
          (println (str f))
          (spit (file (pathify [(str f)])) (substitute-strings content)))))))

(defn create
  [project project-name]
  ;;(println project-name "created!")
  (let [clean-name (clean-proj-name project-name)]
    (binding [*home-dir* (-> (System/getProperty "user.home")
                           (file ".caribou")
                           (.getAbsolutePath))
              *project* project-name
              *project-dir* (-> (System/getProperty "leiningen.original.pwd")
                              (file project-name)
                              (.getAbsolutePath))]
      (println "Creating caribou project:" *project*)
      (if (not= true (.isDirectory (file *home-dir*)))
        (create-config))
      (.mkdirs (file *project-dir*))
      (println "Copying files to: " *project-dir*)
      (unzip-resource "resource_package.zip" *project-dir*)
      (tailor-proj *project-dir*)
      (println "Done...")

      (println "Running bootstrap")
      (let [new-project (project/read (pathify [*project-dir* "project.clj"]))]
        (migrate/migrate new-project (pathify [*project-dir* "resources" "config" "development.clj"])))
      (println "Congratulations! Your project has been provisioned.")
      )))
