(use '[caribou.config :only (read-config configure)])

(def default-config
  {:debug        true
   :use-database true
   :halo-enabled true
   :halo-prefix "/_halo"
   :halo-key    "replace-with-halo-key"
   :halo-host   "http://localhost:33333"

   :database {:classname    "org.postgresql.Driver"
              :subprotocol  "postgresql"
              :host         "localhost"
              :database     "caribou_development"
              :user         "postgres"
              :password     ""}

   :template-dir   "site/resources/templates" 
   :controller-ns  "skel.controllers"
   :public-dir     "site/resources/public"
   :api-public-dir "api/public"
   :asset-dir      "assets"
   })

(defn get-config
  []
  default-config)

;; This call is required by Caribou
(configure (get-config))
