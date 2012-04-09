;;Based off implementation from the noir framework
;;http://webnoir.org/

(ns leiningen.caribou
  (:require [lein_caribou.new :as nnew]))

(defn create [& [project-name]]
  (if-not project-name
    (println "No project name given")
    (nnew/create project-name)))

(defn caribou
  "Creates new caribou projects"
  {:subtasks [#'create]}
  ([subtask & args]
   (case subtask
     "create" (apply create args))))
