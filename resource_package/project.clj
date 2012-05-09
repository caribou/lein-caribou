(defproject antler/caribou-development "0.1.0"
  :description
  "caribou: test site for development purposes"

  :dependencies
   [[org.clojure/clojure "1.3.0"]
    [antler/lein-caribou "1.3.5"]]

  :sub
   ["admin"
    "api"
    "site"]

  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"])
