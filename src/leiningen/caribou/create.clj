;;Provision new caribou project and bootstrap it.

(ns leiningen.caribou.create
  (:require [clojure.string :as string]
            [caribou.model :as model]
            [caribou.db :as db]
            [caribou.config :as config]
            [clojure.java.jdbc :as sql]
            [clojure.pprint :as pprint]
            [leiningen.caribou.migrate :as migrate]
            [leiningen.core.project :as project])
  (:use clojure.java.io
        [zippix.core :only (pathify unzip-resource)]))

(declare ^:dynamic *project* ^:dynamic *project-dir* ^:dynamic *dirs* ^:dynamic *home-dir*)

(defn clean-project-name
  [n]
  (string/replace n #"-" "_"))

(defn get-file
  [n]
  (slurp (pathify n)))

(defn substitution-map
  []
  [[#"\$project\$" *project*]
   [#"\$project-dir\$" *project-dir*]
   [#"\$safeproject\$" (clean-project-name *project*)]])

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

; Is this used anywhere? -kd
; (defn find-db-config
;   [project-name]
;   (config/assoc-subname
;    (dissoc
;     (assoc @config/db :database (str project-name "_development"))
;     :subname)))

;; This is a hack to get around the JVM nonsense of not being able to change the current
;; working directory.  When lein caribou create is first run, the cwd is . as expected,
;; but when it bootstraps the DB using the "migrate" code, this causes a problem
;; for any h2 database, which writes itself into the current directory.
; (defn relocate-bootstrapped-database
;   [new-project project-dir]
;   ;; TODO:kd - don't hardcode the DB name here, derive from bootstrapped config file.
;   (let [bogus-db-file-name (str (clean-project-name (:name new-project)) "_development.h2.db")
;         bogus-db-file      (file bogus-db-file-name)
;         target-db-file     (file (pathify [(:name new-project) bogus-db-file-name]))]
;     (.renameTo bogus-db-file target-db-file)))

(defn evolve-name
  [dir path before after]
  (let [old (file (str dir path before))
        new (file (str dir path after))]
    (.renameTo old new)))

(defn tailor-proj
  [dir]
  (let [clean-project (clean-project-name *project*)
        dir-str (str (file dir))]
    (evolve-name dir-str "/src/" "skel" clean-project)
    (evolve-name dir-str "/resources/cljs/" "skel.cljs" (str *project* ".cljs"))
    (evolve-name dir-str "/resources/public/css/" "skel.css" (str *project* ".css"))
    (evolve-name dir-str "/resources/public/js/" "skel.js" (str *project* ".js")))
  (let [files (file-seq (file dir))]
    (doseq [f files]
      (if (.isFile f)
        (let [content (slurp (str f))]
          (println (str f))
          (spit (file (pathify [(str f)])) (substitute-strings content)))))))

(defn create
  [project project-name]
  ;;(println project-name "created!")
  (let [clean-name (clean-project-name project-name)]
    (binding [*home-dir* (-> (System/getProperty "user.home")
                           (file ".caribou")
                           (.getAbsolutePath))
              *project* project-name
              *project-dir* (-> (System/getProperty "leiningen.original.pwd")
                              (file project-name)
                              (.getAbsolutePath))]
      (println "Creating caribou project:" *project*)
      (.mkdirs (file *project-dir*))
      (println "Copying files to: " *project-dir*)
      (unzip-resource "resource_package.zip" *project-dir*)
      (tailor-proj *project-dir*)
      (println "Done...")

      (println "Running bootstrap")
      (let [new-project (project/read (pathify [*project-dir* "project.clj"]))]
        (migrate/migrate new-project (pathify [*project-dir* "resources" "config" "development.clj"]))
        )
      (println "Congratulations! Your project has been provisioned.")
      )))
