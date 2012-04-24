;;Based off implementation from the noir framework
;;http://webnoir.org/

(ns leiningen.caribou
  (:use leiningen.caribou.create
        leiningen.caribou.bootstrap
        leiningen.caribou.server))

;; ^{:no-project-needed true} 
(defn caribou
  "Creates new caribou projects"
  {:help-arglists '([create bootstrap bootstrap-all start stop])
   :subtasks [#'create bootstrap #'bootstrap-all #'start #'stop]
   :no-project-needed true}
  ([project]
     (println (help-for "caribou")))
  ([project subtask & args]
     (condp = subtask
        "create" (apply create (rest args))
        "bootstrap" (apply bootstrap (rest args))
        "bootstrap-all" (apply bootstrap-all (rest args))
        "start" (apply start (rest args))
        "stop" (apply stop (rest args))
        (println "No command by that name"))))
