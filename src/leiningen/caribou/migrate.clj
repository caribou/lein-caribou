(ns leiningen.caribou.migrate
  (:use caribou.debug [leiningen.caribou.server :only (load-namespaces)])
  (:require [leiningen.core.eval :as eval]
            [caribou.config :as config]
            [caribou.migration :as migration]))

(defn migrate
  [prj config-file & migrations]
  (println "-> Running migrations on" config-file)
  (eval/eval-in-project prj
    `(caribou.migration/run-migrations '~prj '~config-file true '~@migrations)
    (load-namespaces
      'caribou.migration
    ))
  (println "<- Migrations finished."))

(defn rollback
  [prj config-file & rollbacks]
  (eval/eval-in-project prj
    `(caribou.migration/run-rollbacks '~prj '~config-file true '~@rollbacks)
    (load-namespaces
      'caribou.migration
    )))
