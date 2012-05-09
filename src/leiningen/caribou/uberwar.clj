(ns leiningen.caribou.uberwar
  (:require [leiningen.caribou.war :as war]
            [leiningen.ring.war :as ring-war]
            [leiningen.ring.uberwar :as ring-uberwar]
            [leiningen.compile :as compile]
            [clojure.java.io :as io]))

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
     (uberwar project (ring-uberwar/default-uberwar-name project)))
  ([project war-name]
     (let [res (compile/compile project)]
       (when-not (and (number? res) (pos? res))
         (let [war-path (ring-war/war-file-path project war-name)]
           (ring-war/compile-servlet project)
           (if (ring-war/has-listener? project)
             (ring-war/compile-listener project))
           (write-uberwar project war-path)
           (println "Created" war-path)
           war-path)))))

