;;Based of implementation from the noir framework
;;http://webnoir.org/

(ns leiningen.caribou.new
  (:require [clojure.string :as string])
  (:use clojure.java.io))

(declare *project* *project-dir* *dirs*)
(def dir-keys [:css :js :img :views :models :test])

(defn clean-proj-name [n]
  (string/replace n #"-" "_"))

;;From the maginalia source: http://fogus.me/fun/marginalia/
(defn slurp-resource
  [resource-name]
  (try
    (-> (.getContextClassLoader (Thread/currentThread))
        (.getResourceAsStream resource-name)
        (java.io.InputStreamReader.)
        (slurp))
    (catch java.lang.NullPointerException npe
      (println (str "Could not locate resources at " resource-name))
      (println "    ... attempting to fix.")
      (let [resource-name (str "./resources/" resource-name)]
        (try
          (-> (.getContextClassLoader (Thread/currentThread))
              (.getResourceAsStream resource-name)
              (java.io.InputStreamReader.)
              (slurp))
          (catch java.lang.NullPointerException npe
            (println (str "    STILL could not locate resources at " resource-name ". Giving up!"))))))))

(defn get-file [n]
  (slurp-resource n))

(defn get-template [n]
  (let [tmpl (get-file (str "templates/" n))]
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

(defn copy-dir [dir-path]
  (let [files (file-seq (file (str "./resources/" dir-path "/")))]
    (doseq [f (rest files)]
      (def parent-path (string/replace-first (.getParent f) "./resources/" ""))
      (if (.isDirectory f)
        (mkdir [parent-path (.getName f)])
        (->file [parent-path] (.getName f) (get-file (str f)))))))

(defn populate-dirs []
  (->file [] "README" (get-template "README"))
  (->file [] ".gitignore" (get-template "gitignore"))
  (copy-dir "nginx"))
 
(defn create [project-name]
  (println project-name "created!")
  (let [clean-name (clean-proj-name project-name)]
    (binding [*project* project-name
              *project-dir* (-> (System/getProperty "leiningen.original.pwd")
                              (file project-name)
                              (.getAbsolutePath))
              *dirs*{:src ["src" clean-name]
                     :views ["src" clean-name "views"]
                     :models ["src" clean-name "models"]
                     :test ["test" clean-name]
                     :css ["resources" "public" "css"]
                     :img ["resources" "public" "img"]
                     :js ["resources" "public" "js"]}]
      (println "Creating caribou project:" *project*)
      (println "Creating new directories at: " *project-dir*)
      (create-dirs)
      (println "Directories Created")
      (populate-dirs)
      (println "Files Created"))))