;;Based of implementation from the noir framework
;;http://webnoir.org/

(ns leiningen.caribou.new
  (:require [clojure.string :as string])
  (:use clojure.java.io))

(defmacro debug [x]
  `(let [x# ~x] (println "debug:" '~x " -> " x#) x#))

(declare ^:dynamic *project* ^:dynamic *project-dir* ^:dynamic *dirs*)
(def dir-keys [:controllers :migrations :templates])

(defn clean-proj-name [n]
  (string/replace n #"-" "_"))

(defn get-file [n]
  ;(slurp-resource n)
  (slurp n))

(defn get-template [n]
  (let [tmpl (get-file (str "resources/templates/" n))]
    (-> tmpl
      (string/replace #"\$project\$" *project*)
      (string/replace #"\$safeproject\$" (clean-proj-name *project*)))))

(defn get-dir [n]
  (get *dirs* n))

(defn mkdir [args]
  (.mkdirs (apply file *project-dir* args)))

(defn create-dirs[]
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
  (copy-dir "caribou")
  (copy-dir "config")
  (copy-dir "nginx")
  (copy-dir "public")
  (copy-dir "resources"))
 
(defn create [project-name]
  (println project-name "created!")
  (let [clean-name (clean-proj-name project-name)]
    (binding [*project* project-name
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
      (println "Files Created"))))