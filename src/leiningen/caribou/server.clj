(ns leiningen.caribou.server
  (:require [leiningen.core.project :as project]
            [ring.adapter.jetty :as ring]))

(def caribou-servers (ref {}))

(def header-buffer-size 8388608)

(defn full-head-avoidance
  [jetty]
  (doseq [connector (.getConnectors jetty)]
    (.setRequestHeaderSize connector header-buffer-size)))

(def default-jetty-config
  {:port 33003
   :host "127.0.0.1"
   :join? false
   :configurator full-head-avoidance})

(def default-ssl-config
  {:ssl? true
   :ssl-port 33883
   :keystore "caribou.keystore"
   :key-password "caribou"})

(defn start-server
  [project]
  (let [config (project :ring)
        ssl-config (if (config :ssl-port) default-ssl-config {})
        jetty-config (merge default-jetty-config ssl-config)]
    (init)
    (ring/run-jetty
     (var app)
     (merge jetty-config config))))

(def server-datum
  [:frontend :api :admin])
  
(defn server-project-name
  [server-key]
  (str "caribou-" (name server-key) "/project.clj"))

(defn start
  [project]
  (doseq [server-key server-datum]
    (let [server-project (project/read (server-project-name server-key))
          server (start-server server-project)]
      (dosync
       (alter caribou-servers assoc server-key server)))))

(defn stop
  [project]
  (doseq [server caribou-servers]
    (.stop server)))

;; (defn go []
;;   (let [port (Integer/parseInt (or (@config/app :api-port) "33443"))
;;         ssl-port (Integer/parseInt (or (@config/app :api-ssl-port) "33883"))]
;;     (start port ssl-port)))

;; (defn -main []
;;   (go))

