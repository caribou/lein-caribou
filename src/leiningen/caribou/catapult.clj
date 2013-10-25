(ns leiningen.caribou.catapult
  (:require [leiningen.core.eval :as eval]
            [leiningen.caribou.server :as server]))

(defn catapult
  [prj dir config-file]
  (println "Catapulting" dir "to s3")
  (eval/eval-in-project
   prj
   `(let [config# (caribou.config/read-config '~config-file)
          config# (caribou.config/process-config config#)
          config# (caribou.core/init config#)]
      (caribou.core/with-caribou config#
        (caribou.asset/migrate-dir-to-s3 '~dir)))
   (server/load-namespaces 'caribou.config 'caribou.core 'caribou.asset)))
