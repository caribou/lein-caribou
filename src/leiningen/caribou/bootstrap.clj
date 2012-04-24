(ns leiningen.caribou.bootstrap
  (:require [caribou.config :as config]
            [caribou.tasks.bootstrap :as bootstrap]))

(defn database
  [env]
  ((second env) :database))

(defn bootstrap
  [yaml-file environment]
  (let [yaml (config/load-yaml yaml-file)
        env (yaml (keyword environment))]
    (bootstrap/bootstrap (database env))))

(defn bootstrap-all
  [yaml-file]
  (let [yaml (config/load-yaml yaml-file)]
    (doseq [env yaml]
      (bootstrap/bootstrap (database env)))))
  
