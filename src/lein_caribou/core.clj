(ns lein-caribou.core
  (:require [lein_caribou.new :as nnew]))

(defn -main [action & args]
  (case action
     "create" (apply nnew/create args)))
