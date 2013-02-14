(ns leiningen.caribou.release
  (:require [leiningen.core.project :as project]
            [clojure.string :as string]
            [clojure.pprint :as pprint]))

(defn read-project
  [path]
  (let [raw-project (slurp path)
        full-project (read-string raw-project)
        project (into {} (map vec (partition 2 (drop 3 full-project))))
        project-name (nth full-project 1)
        version (nth full-project 2)]
    (assoc project
      :name project-name
      :version version)))

(defn write-project
  [project path]
  (let [project-name (:name project)
        version (:version project)
        values (dissoc project :name :version)
        cat (mapcat identity (seq values))
        output (concat (list 'defproject project-name version) cat)
        raw (with-out-str (pprint/pprint output))]
    (spit path raw)))

(defn increment-version
  [version]
  (let [[major minor patch] (string/split version #"\.")
        patch-number (Integer/parseInt patch)]
    (str major "." minor "." (inc patch-number))))