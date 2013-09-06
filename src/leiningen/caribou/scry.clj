(ns leiningen.caribou.scry
  (:use [leiningen.caribou.server :only (load-namespaces)])
  (:require [leiningen.core.eval :as eval]
            [caribou.config :as config]
            [caribou.migration :as migration]
            [clojure.java.jdbc :as sql]
            [clojure.pprint :as pprint]))

(defn pull-columns
  [cfg]
  (let [columns (into []
                     (sql/resultset-seq
                       (-> (sql/connection)
                           (.getMetaData)
                           (.getColumns nil nil nil nil))))]
    columns))

(defn pull-tables
  [cfg]
  (let [tables (into []
                     (sql/resultset-seq
                       (-> (sql/connection)
                           (.getMetaData)
                           (.getTables nil nil nil (into-array ["TABLE" "VIEW"])))))]
    tables))

(defn build-schema
  [cfg]
  (let [columns (pull-columns cfg)
        tables (reduce #(assoc %1 (keyword (:table_name %2)) %2) {} (pull-tables cfg))
        schema (reduce #(if (contains? tables (keyword (:table_name %2)))
                          (assoc-in %1 [(keyword (:table_name %2)) (keyword (:column_name %2))] %2)
                          %1) {} columns)]
    schema))

(defn scry-schema
  [cfg & [options]]
  (sql/with-connection* (:database cfg) #(build-schema cfg)))

(defn scry-content
  [cfg schema & [options]])

(defn scry
  [prj config-file target-location]
  (println (str "-> import from " config-file " to " target-location))

  (let [cfg (config/process-config (config/read-config config-file))
        schema (scry-schema cfg)
        content (scry-content cfg schema)]
    (pprint/pprint schema))
  (println "<- import finished."))
