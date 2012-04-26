(ns leiningen.caribou.server
  (:require [leiningen.core.project :as project]
            [cemerick.pomegranate :as pom]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as ring]))

(defstruct server-map :server :handler :init :destroy)

(def caribou-servers (ref {}))

(def header-buffer-size 524288)

(defn full-head-avoidance
  [jetty]
  (doseq [connector (.getConnectors jetty)]
    (try
      (.setRequestHeaderSize connector header-buffer-size)
      (catch Exception e
        (.setHeaderBufferSize connector header-buffer-size)))))

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

(defn load-namespaces
  "Create require forms for each of the supplied symbols. This exists because
  Clojure cannot load and use a new namespace in the same eval form."
  [& syms]
  `(require
    ~@(for [s syms :when s]
        `'~(if-let [ns (namespace s)]
             (symbol ns)
             s))))

(defn load-var [sym]
  (when sym
    (require (-> sym namespace symbol))
    (find-var sym)))

;; (defn start-server
;;   [project subdir]
;;   (eval/eval-in-project
;;    (assoc project :eval-in :leiningen)
;;    (let [config (project :ring)
;;          ssl-config (if (config :ssl-port) default-ssl-config {})
;;          jetty-config (merge default-jetty-config ssl-config)
;;          handler (find-var (-> project :ring :handler))
;;          init (find-var (-> project :ring :init))
;;          destroy (try (find-var (-> project :ring :destroy)) (catch Exception e nil))]
;;      (init)
;;      (let [server (ring/run-jetty handler (merge jetty-config config))
;;            server-info (struct server-map server handler init destroy)]
;;        server-info))
;;    (load-namespaces
;;     (-> project :ring :handler)
;;     (-> project :ring :init)
;;     (-> project :ring :destroy))))
  
(defn start-server
  [project]
  (let [config (project :ring)
        ssl-config (if (config :ssl-port) default-ssl-config {})
        jetty-config (merge default-jetty-config ssl-config)
        handler (load-var (-> project :ring :handler))
        init (load-var (-> project :ring :init))
        destroy (load-var (-> project :ring :destroy))]
    (init)
    (let [server (ring/run-jetty handler (merge jetty-config config))
          server-info (struct server-map server handler init destroy)]
      server-info)))
  
(def server-list
  [:site :api :admin])
  
(defn server-project-name
  [server-key]
  (str (name server-key) "/project.clj"))

(defn coordinates-for
  [project]
  [[(symbol (project :name)) (project :version)]])

(defn set-join
  [project join?]
  (update-in project [:ring :join?] (fn [_] join?)))

(defn prepare-server
  [project server-key join?]
  (let [project-name (server-project-name server-key)
        server-project (set-join (project/read project-name) join?)
        project-coordinates (coordinates-for server-project)
        _ (pom/add-classpath (io/file (str (name server-key) "/src")))
        _ (pom/add-dependencies :coordinates (server-project :dependencies) :repositories {"clojars" "http://clojars.org/repo"})
        server-info (start-server server-project)]
    (dosync
     (alter caribou-servers assoc server-key server-info))))

(defn start
  [project]
  (doseq [server-key (butlast server-list)]
    (prepare-server project server-key false))
  (prepare-server project (last server-list) true))

(defn stop
  [project]
  (doseq [server caribou-servers]
    (.stop server)))

