;;Provision new caribou project and bootstrap it.

(ns lein-caribou.new
  (:require [clojure.string :as string]
            [caribou.util :as util]
            [caribou.tasks.bootstrap :as bootstrap]
            [caribou.model :as model]
            [caribou.db :as db]
            [caribou.config :as config]
            [clojure.java.jdbc :as sql])

  (:import [org.apache.commons.io FileUtils]
           [java.util.zip ZipInputStream]
           [java.io File]
           [java.io BufferedOutputStream]
           [java.io FileOutputStream])
  (:use clojure.java.io))

(declare ^:dynamic *project* ^:dynamic *project-dir* ^:dynamic *dirs* ^:dynamic *home-dir*)

(defn clean-proj-name [n]
  (string/replace n #"-" "_"))

(defn get-file [n]
  (slurp (util/pathify n)))

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
  (substitute-strings (get-file [(resource n)])))

(defn mkdir [args]
  (println args)
  (.mkdirs (apply file args)))

(defn create-config []
  (.mkdirs (file *home-dir* "config"))
  (spit (apply file *home-dir* ["config" "database.yml"]) (get-template "database_caribou.yml")))

(defn create-default []
  (model/invoke-models)
  (model/create :page {:name "Home" :path "" :controller "home" :action "home" :template "home.ftl"}))

(defn unzip [zip-file destination]
  (let [zip-stream (ZipInputStream. (clojure.java.io/input-stream (resource zip-file)))]
    ;; iterate over each entry in the zip
    (loop [entry (.getNextEntry zip-stream)]
      (if (not (= nil entry))
        (do (let [out-file (File. (str destination "/" (.getName entry)))]
              ;; blow out directory structure
              (loop [parent-file (.getParentFile out-file)]
                (if (not (= nil parent-file))
                  (do (.mkdirs parent-file)
                      (recur (.getParentFile parent-file)))))
              ;; write dem files
              (if (not (.isDirectory entry))
                (let [out (BufferedOutputStream. (FileOutputStream. out-file) 1024)
                      data (byte-array 1024)]
                  (loop [len (.read zip-stream data 0 1024)]
                    (if (not (= -1 len))
                      (do (.write out data 0 len)
                          (recur (.read zip-stream data 0 1024)))))
                    (.flush out)
                    (.close out))))   
          (recur (.getNextEntry zip-stream)))))
    (.close zip-stream)))

(defn tailor-proj []
  (let [files (file-seq (file *project-dir*))]
    (doseq [f files]
        (if (.isFile f)
          (let [content (slurp (str f))]
          (println (str f))
          (spit (file (util/pathify [(str f)])) (substitute-strings content)))))))

(defn bootstrap-all [yaml-file]
  (let [yaml (config/load-yaml yaml-file)]
    (doseq [env yaml]
      (bootstrap/bootstrap (get (second env) :database)))))
  
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
      (tailor-proj)
      (println "Done...")
      (println "Running bootstrap")
      (bootstrap/bootstrap clean-name)
      (bootstrap/bootstrap (str clean-name "_dev"))
      (bootstrap/bootstrap (str clean-name "_test"))
      (println (str db-config))
      (sql/with-connection db-config (create-default))
      (println "Congratulations! Your project has been provisioned."))))
