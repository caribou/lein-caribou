(ns $project$.core
  (:use [caribou.debug])
  (:require [caribou.config :as caribou-config]
            [caribou.app.handler :as caribou-handler]))

(declare handler)

(defn init
  []
  (caribou-config/init)
  ; Define the app handler, we have to delay this until after the routes are created.
  (def handler (caribou-handler/gen-handler)))
