;;Based off implementation from the noir framework
;;http://webnoir.org/

(ns leiningen.caribou
  (:use [leiningen.help :only (help-for subtask-help-for)]
        leiningen.caribou.create
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
        "create" (apply create args)
        "bootstrap" (apply bootstrap args)
        "bootstrap-all" (apply bootstrap-all args)
        "start" (apply start args)
        "stop" (apply stop args)
        (println "No command by that name"))))
