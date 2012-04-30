(defproject caribou-devsite "0.1.0-SNAPSHOT"
  :description "The page routing ring handler for caribou"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [antler/caribou-frontend "0.3.3"]]
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"]
  :ring {:handler $project$.core/handler
         :servlet-name "caribou-development-frontend"
         :init $project$.core/init
         :port 33333})
