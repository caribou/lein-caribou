(ns leiningen.caribou.migrate
  (:require [leiningen.core.eval :as eval]
            [leiningen.caribou.util :refer [load-namespaces
                                            retrieve-config-and-args]]
            [caribou.config :as config]
            [caribou.migration :as migration]))

(defn migrate
  [prj & args]
  (let [[[namespace callback] migrations] (retrieve-config-and-args prj args)]
    (println "-> Running migrations on config")
    (if (not (nil? prj))
      (eval/eval-in-project prj
                            `(caribou.migration/run-migrations '~prj (~callback) true '~@migrations)
                            (load-namespaces
                              'caribou.migration
                              namespace
                             ))
      (apply caribou.migration/run-migrations (concat [prj (callback) true] migrations))))
  (println "<- Migrations finished."))

(defn rollback
  [prj & args]
  (let [[[namespace callback] rollbacks] (retrieve-config-and-args prj args)]
    (println "-> Running rollback on config")
    (if (not (nil? prj))
      (eval/eval-in-project prj
                            `(caribou.migration/run-rollbacks '~prj (~callback) true '~@rollbacks)
                            (load-namespaces
                              'caribou.migration
                              namespace
                             ))
      (apply caribou.migration/run-rollbacks (concat [prj (callback) true] rollbacks))))
  (println "<- Rollbacks finished."))




