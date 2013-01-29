;;Based off implementation from the noir framework
;;http://webnoir.org/

(ns leiningen.caribou
  (:use [leiningen.help :only (help-for subtask-help-for)]
        leiningen.caribou.create
        leiningen.caribou.migrate
        leiningen.caribou.server
        leiningen.caribou.war
        leiningen.caribou.uberwar
        leiningen.caribou.versions))
      

;; ^{:no-project-needed true} 
(defn caribou
  "Creates new caribou projects"
  {:help-arglists '([create migrate rollback start stop uberwar uberwar-all])
   :subtasks [#'create #'migrate #'rollback #'start #'stop #'uberwar #'uberwar-all]
   :no-project-needed true}
  ([project]
     (println (help-for "caribou")))
  ([project subtask & args]
     (let [subtask-args (cons project args)]
       (condp = subtask
         "create" (apply create subtask-args)
         "migrate" (apply migrate subtask-args)
         "rollback" (apply rollback subtask-args)
         "start" (apply start subtask-args)
         "stop" (apply stop subtask-args)
         "uberwar" (apply uberwar subtask-args)
         "uberwar-all" (apply uberwar-all subtask-args)
         ;;"versions" (apply versions subtask-args)
         (println "No command by that name")))))
