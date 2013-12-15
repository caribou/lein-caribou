(ns leiningen.caribou.migrate
  (:require [leiningen.core.eval :as eval]
            [leiningen.caribou.util :refer [load-namespaces
                                            retrieve-config-and-args]]
            [caribou.config :as config]
            [caribou.migration :as migration]))

(defn migrate
  [prj & args]
  (let [[config migrations] (retrieve-config-and-args prj args)]
    (println "-> Running migrations on config")
    (if (not (nil? prj))
      (eval/eval-in-project prj
                            `(caribou.migration/run-migrations '~prj '~config true '~@migrations)
                            (load-namespaces
                             'caribou.migration
                             ))
      (apply caribou.migration/run-migrations (concat [prj config true] migrations))))
  (println "<- Migrations finished."))

(defn rollback
  [prj & args]
  (let [[config rollbacks] (retrieve-config-and-args prj args)]
    (println "-> Running rollback on config")
    (if (not (nil? prj))
      (eval/eval-in-project prj
                            `(caribou.migration/run-rollbacks '~prj '~config true '~@rollbacks)
                            (load-namespaces
                             'caribou.migration
                             ))
      (apply caribou.migration/run-rollbacks (concat [prj config true] rollbacks))))
  (println "<- Rollbacks finished."))




