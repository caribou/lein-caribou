;;Based off implementation from the noir framework
;;http://webnoir.org/

(ns leiningen.caribou
  (:require [lein-caribou.new :as nnew]))

(defn create [& [project-name]]
  (if-not project-name
    (println "No project name given")
    (nnew/create project-name)))

(defn bootstrap [& [yaml-file]]
  (nnew/bootstrap-all yaml-file))

(defn ^{:no-project-needed true} caribou
  "Creates new caribou projects"
  ([& args]
     (let [args (rest args)]
       (cond
        (= "create" (first args)) (apply create (rest args))
        (= "bootstrap" (first args)) (apply bootstrap (rest args))
        :else (println "No command by that name")))))
