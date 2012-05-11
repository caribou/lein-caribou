(defproject antler/lein-caribou "1.4.1"
  :description "Caribou Provisioning Tool"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [leiningen-core "2.0.0-preview3"]
                 [ring/ring-core "1.1.0"
                  :exclusions [org.clojure/clojure
                               clj-stacktrace
                               hiccup]]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [com.cemerick/pomegranate "0.0.11"]
                 [antler/zippix "0.1.0"]
                 [antler/commons-io "2.2.0"]
                 [antler/caribou-core "0.5.4"]]
  :dev-dependencies [[lein-eclipse "1.0.0"]
                     [lein-clojars "0.6.0"]]
  :eval-in-leiningen true
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"]
  :repositories {"snapshots" {:url "http://battlecat:8080/nexus/content/repositories/snapshots" 
                              :username "deployment" :password "deployment"}
                 "releases"  {:url "http://battlecat:8080/nexus/content/repositories/releases" 
                              :username "deployment" :password "deployment"}})
