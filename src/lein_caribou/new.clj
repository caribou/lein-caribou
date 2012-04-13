;;Based of implementation from the noir framework
;;http://webnoir.org/

(ns lein-caribou.new
  (:require [clojure.string :as string]
            [caribou.util :as util]
            [caribou.tasks.bootstrap :as bootstrap]
            [caribou.model :as model]
            [caribou.db :as db]
            [caribou.app.config :as config]
            [clojure.java.jdbc :as sql])

  (:import [org.apache.commons.io FileUtils])
  (:use clojure.java.io))

(declare ^:dynamic *project* ^:dynamic *project-dir* ^:dynamic *dirs* ^:dynamic *home-dir*)

(defn clean-proj-name [n]
  (string/replace n #"-" "_"))

(defn get-file [n]
  (slurp (resource (util/pathify n))))

(defn substitute-strings [tmpl]
  (-> tmpl
    (string/replace #"\$project\$" *project*)
    (string/replace #"\$project-dir\$" *project-dir*)
    (string/replace #"\$safeproject\$" (clean-proj-name *project*))))

(defn get-template [n]
  (substitute-strings (get-file ["templates" n])))

(defn get-dir [n]
  (get *dirs* n))

(defn mkdir [args]
  (.mkdirs (apply file *project-dir* args)))

(defn create-config []
  (.mkdirs (file *home-dir* "config"))
  (spit (apply file *home-dir* ["config" "database.yml"]) (get-template "database_caribou.yml")))

(defn create-dirs []
  (if (not= true (.isDirectory (file *home-dir*)))
    (create-config))
  (doseq [dir (vals *dirs*)]
    (mkdir dir)))

(defn ->file [path file-name content]
  (let [target (util/pathify (concat [*project-dir*] path [file-name]))]
    (spit (file target) content)))

(defn re-replace-beginning
  [r s]
  (let [[_ after] (re-find r s)]
    after))

(defn copy-dir [dir-path]
  (FileUtils/copyDirectory (resource dir-path) (file *project-dir* dir-path) true))

(defn copy-resource
  [path r]
  (->file path r (get-file (concat path [r]))))

(defn create-default []
  (model/invoke-models)
  (model/create :page {:name "Home" :path "" :controller "home" :action "home" :template "home.ftl"}))

(defn copy-bootstrap [] 
  (copy-resource ["public" "js"] "application.js")
  (copy-resource ["public" "js"] "bootstrap-alert.js")
  (copy-resource ["public" "js"] "bootstrap-button.js")
  (copy-resource ["public" "js"] "bootstrap-carousel.js")
  (copy-resource ["public" "js"] "bootstrap-collapse.js")
  (copy-resource ["public" "js"] "bootstrap-dropdown.js")
  (copy-resource ["public" "js"] "bootstrap-modal.js")
  (copy-resource ["public" "js"] "bootstrap-popover.js")
  (copy-resource ["public" "js"] "bootstrap-scrollspy.js")
  (copy-resource ["public" "js"] "bootstrap-tab.js")
  (copy-resource ["public" "js"] "bootstrap-tooltip.js")
  (copy-resource ["public" "js"] "bootstrap-transition.js")
  (copy-resource ["public" "js"] "bootstrap-typeahead.js")
  (copy-resource ["public" "js"] "jquery.js")
  (copy-resource ["public" "css"] "bootstrap.css")
  (copy-resource ["public" "css"] "bootstrap-responsive.css")
  (copy-resource ["public" "ico"] "apple-touch-icon-114-precomposed.png")
  (copy-resource ["public" "ico"] "apple-touch-icon-57-precomposed.png")
  (copy-resource ["public" "ico"] "apple-touch-icon-72-precomposed.png"))

(defn populate-dirs []
  (->file [] "README" (get-template "README"))
  (->file [] ".gitignore" (get-template "gitignore"))
  (->file [] "caribou.keystore" (get-template "caribou.keystore"))
  (->file [] "project.clj" (get-template "project.clj"))
  (->file (get-dir :src) "core.clj" (get-template "core.clj"))
  (->file ["config"] "database.yml" (get-template "database.yml"))
  (copy-resource ["config"] "type-specs.json")
  (copy-resource ["public" "cors"] "index.html")
  (copy-resource ["public"] "easyXDM.min.js")
  (copy-resource ["public"] "easyxdm.swf")
  (copy-resource ["public"] "json2.js")
  (copy-resource ["public"] "name.html")
  (copy-resource ["public"] "upload_rpc.html")
  (copy-resource ["resources"] "caribou.properties")
  (copy-bootstrap)
  (->file (get-dir :controllers) "home_controller.clj" (get-template "home_controller.clj"))
  (->file (get-dir :templates) "home.ftl" (get-template "home.ftl"))
  (->file ["nginx"] "nginx.conf" (get-template "nginx.conf")))
 
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
                              (.getAbsolutePath))
              *dirs* {:src ["src" clean-name]
                      :controllers ["app" "controllers"]
                      :migrations ["app" "migrations"]
                      :templates ["app" "templates"]
                      :config ["config"]
                      :cors ["public" "cors"]
                      :img ["public" "img"]
                      :ico ["public" "ico"]
                      :js ["public" "js"]
                      :css ["public" "css"]
                      :nginx ["nginx"]
                      :resources ["resources"]}]
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
      (println (str db-config))
      (sql/with-connection db-config (create-default))
      (println "Congratulations! Your project has been provisioned."))))
