(defproject $project$ "0.1.0"
  :description
  "caribou: type structure interaction medium"

  :dependencies
  [[antler/caribou-core "0.4.5"]]

  :sub
   ["caribou-api"
   "caribou-frontend"
   "caribou-admin"]

  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"])
