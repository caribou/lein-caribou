;;Based off implementation from the noir framework
;;http://webnoir.org/

(ns leiningen.caribou
  (:require [leiningen.caribou.new :as nnew]))

(defn create [& [project-name]]
  (if-not project-name
    (println "No project name given")
    (nnew/create project-name)))

(defn test-me []
  (println "added another line")
  (println "this is my test biotch what?"))
