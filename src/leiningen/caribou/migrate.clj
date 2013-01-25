(ns leiningen.caribou.migrate
  (:use caribou.debug [leiningen.caribou.server :only (load-namespaces)])
  (:require [leiningen.core.eval :as eval]
            [caribou.config :as config]
            [caribou.migration :as migration]))

(defn migrate
  [prj config-file & migration]
  (eval/eval-in-project prj
    `(caribou.migration/run-migrations '~prj ~config-file ~migration)
    (load-namespaces
      'caribou.migration
    )))

(defn rollback
  [prj config-file & rollback]
  (eval/eval-in-project prj
    `(caribou.migration/run-rollbacks '~prj ~config-file ~rollback)
    (load-namespaces
      'caribou.migration
    )))
