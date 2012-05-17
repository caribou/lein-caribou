(ns leiningen.caribou.bootstrap
  (:use caribou.debug)
  (:require [caribou.config :as config]
            [caribou.tasks.bootstrap :as bootstrap]))

(defn bootstrap
  [_ config-file]
  (let [config (config/read-config config-file)
        _ (config/configure config)
        database (config/assoc-subname (config :database))]
    (config/configure config)
    (bootstrap/bootstrap database)))

;; (defn bootstrap-all
;;   []
;;   (let [config (config/read-config config-file)]
;;     (doseq [env yaml]
;;       (bootstrap/bootstrap (database env)))))
  
