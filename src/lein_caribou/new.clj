;;Based of implementation from the noir framework
;;http://webnoir.org/

(ns lein_caribou.new
  (:require [clojure.string :as string]
            [caribou.tasks.bootstrap :as bootstrap])
  (:use clojure.java.io))

(defmacro debug [x]
  `(let [x# ~x] (println "debug:" '~x " -> " x#) x#))

(declare ^:dynamic *project* ^:dynamic *project-dir* ^:dynamic *dirs* ^:dynamic *home-dir*)
(def dir-keys [:src :controllers :migrations :templates])

(defn clean-proj-name [n]
  (string/replace n #"-" "_"))

(defn get-file [n]
  ;(slurp-resource n)
  (slurp n))

(defn substitute-strings [tmpl]
  (-> tmpl
    (string/replace #"\$project\$" *project*)
    (string/replace #"\$project-dir\$" *project-dir*)
    (string/replace #"\$safeproject\$" (clean-proj-name *project*))))

(defn get-template [n]
  (substitute-strings (get-file (str "resources/templates/" n))))

(defn get-dir [n]
  (get *dirs* n))

(defn mkdir [args]
  (.mkdirs (apply file *project-dir* args)))

(defn create-config []
  (.mkdirs (file *home-dir* "config"))
  (spit (apply file *home-dir* ["config" "database.yml"]) (get-template "database_caribou.yml")))

(defn create-dirs[]
  (if (not= true (.isDirectory (file *home-dir*)))
    (create-config))
  (doseq [k dir-keys]
    (mkdir (get-dir k))))

(defn ->file [path file-name content]
  (spit (apply file *project-dir* (conj path file-name)) content))

(defn re-replace-beginning
  [r s]
  (let [[_ after] (re-find r s)]
    after))

(defn copy-dir [dir-path]
  (try
  (let [files (file-seq (file (str "resources/" dir-path "/")))]
    (doseq [f files]
      (def parent-path (re-replace-beginning #"^resources/?(.*)" (.getParent f)))
      (if (.isDirectory f)
        (mkdir [parent-path (.getName f)])
        (->file [parent-path] (.getName f) (get-file (str f))))))
  (catch Exception e (.printStackTrace e))))

(defn populate-dirs []
  (->file [] "README" (get-template "README"))
  (->file [] ".gitignore" (get-template "gitignore"))
  (->file [] "caribou.keystore" (get-template "caribou.keystore"))
  (->file [] "project.clj" (get-template "project.clj"))
  (->file (get-dir :src) "core.clj" (get-template "core.clj"))
  (copy-dir "caribou")
  (copy-dir "config")
  (copy-dir "nginx")
  (copy-dir "public")
  (copy-dir "resources")
  (->file ["config"] "database.yml" (get-template "database.yml"))
  (->file ["nginx"] "nginx.conf" (get-template "nginx.conf")))
 
(defn create [project-name]
  (println project-name "created!")
  (let [clean-name (clean-proj-name project-name)]
    (binding [*home-dir* (-> (System/getProperty "user.home")
                           (file ".caribou")
                           (.getAbsolutePath))
              *project* project-name
              *project-dir* (-> (System/getProperty "leiningen.original.pwd")
                              (file project-name)
                              (.getAbsolutePath))
              *dirs*{:src ["src" clean-name]
                     :controllers ["app" "controllers"]
                     :migrations ["app" "migrations"]
                     :templates ["app" "templates"]}]
      (println "Creating caribou project:" *project*)
      (println "Creating new directories at: " *project-dir*)
      (create-dirs)
      (println "Directories Created")
      (populate-dirs)
      (println "Files Created")
      (println "Running bootstrap")
      (bootstrap/bootstrap clean-name)
      (bootstrap/bootstrap (str clean-name "_dev"))
      (bootstrap/bootstrap (str clean-name "_test"))
      (println "Congratulations! Your project has been provisioned."))))
