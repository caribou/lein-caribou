(defproject caribou-development-admin "0.1.0-SNAPSHOT"
  :description "Flexible and adaptive admin for caribou-api"
  :url "http://github.com/antler/caribou-admin"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [antler/caribou-admin "0.3.5"]]
  :ring {:handler caribou.admin.core/app
         :servlet-name "caribou-development-admin"
         :init caribou.admin.core/init
         :port 33553})

