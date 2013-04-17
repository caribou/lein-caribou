(defproject antler/lein-caribou "2.1.4"
  :description "Caribou Provisioning Tool"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [leiningen-core "2.0.0"]
                 [ring "1.1.6"
                  :exclusions [org.clojure/clojure
                               clj-stacktrace
                               hiccup]]
                 [lein-ring "0.7.1"]
                 ;; [ring/ring-servlet "1.1.0"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [com.cemerick/pomegranate "0.0.11"]
                 [antler/zippix "0.1.0"]
                 [antler/commons-io "2.2.0"]
                 [antler/caribou-core "0.10.0"]]
  :eval-in-leiningen true
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"])
