(ns leiningen.caribou.util
  (:require [leiningen.core.eval :as eval]
            [clojure.string :as string]
            [caribou.config :as config]))

(defn load-namespaces
  "Create require forms for each of the supplied symbols. This exists because
  Clojure cannot load and use a new namespace in the same eval form."
  [& syms]
  `(require
    ~@(for [s syms :when s]
        `'~(if-let [ns (namespace s)]
             (symbol ns)
             s))))

(defn read-static-config [config-file]
  ['caribou.config `#(-> ~config-file
                         caribou.config/read-config
                         caribou.config/process-config)])

(defn load-boot
  [prj boot-location]
  (let [namespace (symbol (first (string/split boot-location #"/")))
        boot-fn (symbol boot-location)]
    [namespace boot-fn]))

(defn retrieve-config-and-args
  "Chooses a config loading fn based upon the :static or :boot option
   and retuns a vector where the first element is a vector with the required
   namespaces in the first position and the config loading function in the
   second. The second element of the top level vector is the rest of the args
   passed in."
  [prj args]
  (condp = (first args)
    ":static" [(read-static-config (second args))
               (nthrest args 2)]
    ":boot" [(load-boot prj (second args))
             (nthrest args 2)]
    [(read-static-config (first args))
     (rest args)]))
