(defproject caribou/lein-caribou "2.4.8"
  :description "Caribou Provisioning Tool"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [leiningen-core "2.0.0"]
                 [ring "1.2.0"
                  :exclusions [org.clojure/clojure
                               clj-stacktrace
                               hiccup]]
                 [lein-ring "0.8.6"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [com.cemerick/pomegranate "0.0.11"]
                 [antler/zippix "0.1.0"]
                 [commons-io "2.2"]
                 [caribou/caribou-core "0.12.14"]]
  :eval-in-leiningen true
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"])
