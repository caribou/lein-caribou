(ns leiningen.caribou.war
  (:require leiningen.deps
            [leiningen.compile :as compile]
            [leiningen.ring.war :as ring-war]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:use [leiningen.ring.server :only (eval-in-project)]
        [clojure.data.xml :only [sexp-as-element indent-str]])
  (:import [java.util.jar Manifest
                          JarEntry
                          JarOutputStream]
           [java.io BufferedOutputStream 
                    FileOutputStream 
                    ByteArrayInputStream]))

(defn file-entry [war project war-path file]
  (when (and (.exists file)
             (.isFile file)
             (not (ring-war/skip-file? project war-path file)))
    (try (ring-war/write-entry war war-path file)
    (catch Exception e (println (str "Tried to add duplicate file, skipping: " file))))))

(defn dir-entry [war project war-root dir-path]
  (doseq [file (file-seq (io/file dir-path))]
    (let [war-path (ring-war/in-war-path war-root dir-path file)]
      (file-entry war project war-path file))))

(defn war-file-path [project war-name]
  (let [target-dir (or (:target-dir project) (:target-path project))]
    (.mkdirs (io/file target-dir))
    (str target-dir "/" war-name)))

(defn write-war [project war-path]
  (with-open [war-stream (ring-war/create-war project war-path)]
    (doto war-stream
      (ring-war/str-entry "WEB-INF/web.xml" (ring-war/make-web-xml project))
      (dir-entry project "WEB-INF/classes/" (:compile-path project)))
    (doseq [path (concat [(:source-path project)] (:source-paths project)
                         [(:resources-path project)] (:resource-paths project))
            :when path]
      (dir-entry war-stream project "WEB-INF/classes/" path))
    (dir-entry war-stream project "" (ring-war/war-resources-path project))
    war-stream))

(defn war
  "Create a $PROJECT-$VERSION.war file."
  ([project]
     (war project (ring-war/default-war-name project)))
  ([project war-name]
     (let [res (compile/compile project)]
       (when-not (and (number? res) (pos? res))
         (let [war-path (war-file-path project war-name)]
           (ring-war/compile-servlet project)
           (if (ring-war/has-listener? project)
             (ring-war/compile-listener project))
           (write-war project war-path)
           (println "Created" war-path)
           war-path)))))

