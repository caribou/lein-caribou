(defproject antler/lein-caribou "1.2.1"
  :description "Caribou Provisioning Tool"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [antler/zippix "0.1.0"]
                 [antler/commons-io "2.2.0"]
                 [antler/caribou-core "0.5.0"]]
  :dev-dependencies [[lein-eclipse "1.0.0"]
                     [lein-clojars "0.6.0"]]
  :eval-in-leiningen true
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"])
