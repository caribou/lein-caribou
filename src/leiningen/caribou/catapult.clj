(ns leiningen.caribou.catapult
  (:require [caribou.config :as config]
            [caribou.asset :as asset]))

(defn catapult
  [prj dir bucket prefix]
  (config/init)
  (asset/migrate-dir-to-s3 dir bucket prefix))