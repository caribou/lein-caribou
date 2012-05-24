(ns leiningen.caribou.uberwar
  (:require [leiningen.caribou.war :as war]
            [leiningen.ring.war :as ring-war]
            [leiningen.ring.uberwar :as ring-uberwar]
            [leiningen.core.project :as project]            
            [leiningen.compile :as compile]
            [clojure.java.io :as io]))

(def subproject-list
  [:site :api :admin])
  
(defn subproject-name
  [project-key]
  (str (name project-key) "/project.clj"))

(defn write-uberwar [project war-path]
  (with-open [war-stream (ring-war/create-war project war-path)]
    (doto war-stream
      (ring-war/str-entry "WEB-INF/web.xml" (ring-war/make-web-xml project))
      (war/dir-entry project "WEB-INF/classes/" (:compile-path project)))
    (doseq [path (concat [(:source-path project)] (:source-paths project)
                         [(:resources-path project)] (:resource-paths project))
            :when path]
    (war/dir-entry war-stream project "WEB-INF/classes/" path))
    (war/dir-entry war-stream project "" (ring-war/war-resources-path project))
    (ring-uberwar/jar-entries war-stream project)))

(defn uberwar
  "Create a $PROJECT-$VERSION.war with dependencies."
  ([project]
     (let [res (compile/compile project)
           war-name (ring-uberwar/default-uberwar-name project)]
       (when-not (and (number? res) (pos? res))
         (let [war-path (war/war-file-path project war-name)]
           (ring-war/compile-servlet project)
           (if (ring-war/has-listener? project)
             (ring-war/compile-listener project))
           (write-uberwar project war-path)
           (println "Created" war-path)
           war-name)))))

(defn uberwar-all
  "Build $PROJECT-$VERSION.war for all subprojects"
  [project]
  (doseq [project-key subproject-list]
    (let [project-name (subproject-name project-key)
          subproject (project/read project-name)
          war-name (uberwar subproject)]
      (io/copy (io/file (str (:target-path subproject) "/" war-name))
               (io/file (str (:target-path project) "/" war-name)))
        )))

