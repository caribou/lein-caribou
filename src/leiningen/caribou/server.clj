(ns leiningen.caribou.server
  (:require [leiningen.core.project :as project]
            [ring.adapter.jetty :as ring]))

(defstruct server-map :server :handler :init :destroy)

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

(defn load-var [sym]
  (when sym
    (require (-> sym namespace symbol))
    (find-var sym)))

(defn start-server
  [project]
  (let [config (project :ring)
        ssl-config (if (config :ssl-port) default-ssl-config {})
        jetty-config (merge default-jetty-config ssl-config)
        handler (load-var (-> config :ring :handler))
        init (load-var (-> config :ring :init))
        destroy (load-var (-> config :ring :destroy))]
    (init)
    (let [server (ring/run-jetty handler (merge jetty-config config))
          server-info (server-map server handler init destroy)]
      server-info)))

(def server-datum
  [:frontend :api :admin])
  
(defn server-project-name
  [server-key]
  (str "caribou-" (name server-key) "/project.clj"))

(defn start
  [project]
  (doseq [server-key server-datum]
    (let [server-project (project/read (server-project-name server-key))
          server-info (start-server server-project)]
      (dosync
       (alter caribou-servers assoc server-key server-info)))))

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

