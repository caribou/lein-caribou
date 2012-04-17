(defproject $project$-api "0.1.0-SNAPSHOT"
  :description "The api ring handler for caribou"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [antler/caribou-api "0.2.2"]]
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"]
  :ring {:handler caribou.api.core/app
         :servlet-name "$project$-api"
         :init caribou.api.core/init
         :port 33443})