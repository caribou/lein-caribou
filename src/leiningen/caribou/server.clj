(ns leiningen.caribou.server
  (:require [leiningen.core.project :as project]
            [leiningen.core.eval :as eval]
            [cemerick.pomegranate :as pom]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as ring]))

(defn load-namespaces
  "Create require forms for each of the supplied symbols. This exists because
  Clojure cannot load and use a new namespace in the same eval form."
  [& syms]
  `(require
    ~@(for [s syms :when s]
        `'~(if-let [ns (namespace s)]
             (symbol ns)
             s))))

(defn full-head-avoidance
  [jetty]
  (doseq [connector (.getConnectors jetty)]
    (.setRequestHeaderSize connector 8388608)))

(def server-list
  [:site :api :admin])
  
(defn server-project-name
  [server-key]
  (str (name server-key) "/project.clj"))

(defn server-task
  [project options]
  (let [project (update-in project [:ring] merge options)]
    (eval/eval-in-project
     (update-in project [:dependencies] concat [['antler/ring-server "0.2.3"]])
     `(ring.server.leiningen/serve '~project)
     (load-namespaces
      'ring.server.leiningen
      (-> project :ring :handler)
      (-> project :ring :init)
      (-> project :ring :destroy)))))

(defn start-server
  [server-key]
  (let [project-name (server-project-name server-key)
        subproject (project/read project-name)]
    (server-task subproject {:open-browser? false :join false})))

(defn start
  [project]
  (doseq [server-key server-list]
    (let [project-name (server-project-name server-key)
          join? (= server-key (last server-list))
          subproject (project/read project-name)]
      (if (not join?)
        (.start (Thread. #(server-task subproject {:open-browser? false})))
        (server-task subproject {:open-browser? false})))))
        
(defn stop
  [project])

