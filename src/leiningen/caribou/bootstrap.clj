(ns leiningen.caribou.bootstrap
  (:require [caribou.config :as config]
            [caribou.tasks.bootstrap :as bootstrap]))

(defn bootstrap
  [_ config-file]
  (let [config (config/read-config config-file)
        database (-> config :database)]
    (bootstrap/bootstrap database)))

;; (defn bootstrap-all
;;   []
;;   (let [config (config/read-config config-file)]
;;     (doseq [env yaml]
;;       (bootstrap/bootstrap (database env)))))
  
