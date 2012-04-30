(ns $project$.controllers.home
  (:use caribou.debug
        [cheshire.core :only (generate-string)])
  (:require [clj-time.core :as time-core]
            [clj-time.format :as format]
            [clj-time.coerce :as coerce]
            [caribou.db :as db]
            [caribou.model :as model]
            [caribou.config :as config]))

(defn home
  [params]
  (let [template (params :template)]
    (template params)))
