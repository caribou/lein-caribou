(ns leiningen.caribou.catapult
  (:require [leiningen.core.eval :as eval]
            [leiningen.caribou.util :as util]))

(defn catapult
  [prj dir & args]
  (println "Catapulting" dir "to s3")
  (let [[[namespace callback]] (util/retrieve-config-and-args args)]
    (eval/eval-in-project
     prj
    `(let [config# (caribou.core/init (~callback))]
       (caribou.core/with-caribou config#
         (caribou.asset/migrate-dir-to-s3 '~dir)))
    (util/load-namespaces 'caribou.config 'caribou.core 'caribou.asset namespace))))
