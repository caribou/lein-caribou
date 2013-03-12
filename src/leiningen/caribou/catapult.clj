(ns leiningen.caribou.catapult
  (:require [leiningen.core.eval :as eval]
            [leiningen.caribou.server :as server]))

(defn catapult
  [prj dir bucket prefix]
  (println "Catapulting" dir "to the" bucket "s3 bucket under" prefix)
  (eval/eval-in-project
   prj
   `(do
      (caribou.config/init)
      (caribou.asset/migrate-dir-to-s3 '~dir '~bucket '~prefix))
   (server/load-namespaces 'caribou.config 'caribou.asset)))