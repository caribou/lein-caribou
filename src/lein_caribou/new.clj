;;Based of implementation from the noir framework
;;http://webnoir.org/

(ns lein-caribou.new
  (:require [clojure.string :as string]
            [caribou.util :as util]
            [caribou.tasks.bootstrap :as bootstrap]
            [caribou.model :as model]
            [caribou.db :as db]
            [caribou.config :as config]
            [clojure.java.jdbc :as sql])

  (:import [org.apache.commons.io FileUtils])
  (:use clojure.java.io))

(declare ^:dynamic *project* ^:dynamic *project-dir* ^:dynamic *dirs* ^:dynamic *home-dir*)

(defn clean-proj-name [n]
  (string/replace n #"-" "_"))

(defn get-file [n]
  (slurp (resource (util/pathify n))))

(def substitution-map
     [[#"\$project\$" *project*]
      [#"\$project-dir\$" *project-dir*]
      [#"\$safeproject\$" (clean-proj-name *project*)]])

(defn sub-strings [tmpl]
  (reduce 
    (fn [t pair] (string/replace t (first pair) (last pair))) tmpl substitution-map))

(defn substitute-strings [tmpl]
  (-> tmpl
    (string/replace #"\$project\$" *project*)
    (string/replace #"\$project-dir\$" *project-dir*)
    (string/replace #"\$safeproject\$" (clean-proj-name *project*))))

(defn get-template [n]
  (substitute-strings (get-file ["templates" n])))

(defn mkdir [args]
  (println args)
  (.mkdirs (apply file args)))

(defn create-config []
  (.mkdirs (file *home-dir* "config"))
  (spit (apply file *home-dir* ["config" "database.yml"]) (get-template "database_caribou.yml")))

(defn ->file [path file-name content]
  (let [target (util/pathify (concat [*project-dir*] path [file-name]))]
    (spit (file target) content)))

(defn re-replace-beginning
  [r s]
  (let [[_ after] (re-find r s)]
    after))

(defn copy-dir [dir-path]
  (FileUtils/copyDirectory (resource dir-path) (file *project-dir* dir-path) true))

(defn create-default []
  (model/invoke-models)
  (model/create :page {:name "Home" :path "" :controller "home" :action "home" :template "home.ftl"}))

(defn load-resource
  [resource-name]
  (let [thr (Thread/currentThread)
        ldr (.getContextClassLoader thr)]
    (.getResourceAsStream ldr resource-name)))

(defn mkzip [zip-file destination]
  (println zip-file)
  (println destination)
    
  (let [z (input-stream (resource zip-file))
        f (file (str destination "/" zip-file))
        out (java.io.FileOutputStream. f)
        ;;out (java.util.zip.ZipOutputStream. (java.io.FileOutputStream. f))
        ;;out (load-resource "resources/resource_package.zip")
         bytez (byte-array 1024)] 
      (loop [len (.read z bytez 0 1024)]
         (if (not (= -1 len))
             (do (.write out bytez 0 1024) 
                 (recur (.read z bytez 0 1024)))))
         (.close out)
         (file f)
   ))

(defn copy-zip [zip-file destination]
  (println zip-file)
  (println destination)
  (fn [entry]
    (let [file-name (.getName entry)
          content (slurp (.getInputStream zip-file entry))]
      (println file-name)
      (if (.isDirectory entry)
        (mkdir [destination file-name])
        (spit (file (util/pathify [destination file-name])) (substitute-strings content))))))

(defn unzip [zip-file destination]
  ;;(let [z (java.util.zip.ZipFile. (.getFile (resource zip-file)))] 
  (let [z (java.util.zip.ZipFile. (mkzip zip-file destination))] 
    (doall (map (copy-zip z destination) (enumeration-seq (.entries z))))))
  
(defn create [project-name]
  (println project-name "created!")
  (let [clean-name (clean-proj-name project-name)
        db-config (config/assoc-subname (dissoc (assoc (config/all-db :development) :database (str clean-name "_dev")) :subname))]
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
      (unzip "resource_package.zip" *project-dir*)
      (println "Done...")
      (println "Running bootstrap")
      ;(bootstrap/bootstrap clean-name)
      ;(bootstrap/bootstrap (str clean-name "_dev"))
      ;(bootstrap/bootstrap (str clean-name "_test"))
      (println (str db-config))
      ;(sql/with-connection db-config (create-default))
      (println "Congratulations! Your project has been provisioned."))))
