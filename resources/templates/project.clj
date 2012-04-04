(defproject $project$ "1.0.0-SNAPSHOT"
  :description "Caribou: $project$"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [antler/caribou-frontend "0.1.0-SNAPSHOT"]]
  :dev-dependencies [[lein-ring "0.6.3"]
                     [swank-clojure "1.4.0-SNAPSHOT"]]
  :ring {:handler caribou-frontend.core/app
         :servlet-name "$project$-frontend"
         :init caribou-frontend.core/init})
