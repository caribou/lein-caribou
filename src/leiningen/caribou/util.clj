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
  (-> config-file
      config/read-config
      config/process-config))

(defn load-boot
  [prj boot-location]
  (print (let [namespace (symbol (first (string/split boot-location #"/")))
         boot-fn (symbol boot-location)]
     (eval/eval-in-project prj
                           `(~boot-fn)
                           `(require '~namespace)))))

(defn retrieve-config-and-args
  "Chooses a config loading fn based upon the :static or :boot option
   and retuns a config map as the first element and the rest of the
   args as the second"
  [prj args]
  (condp = (first args)
    ":static" [(read-static-config (second args))
               (nthrest args 2)]
    ":boot" [(load-boot prj (second args))
             (nthrest args 2)]
    [(read-static-config (first args))
     (rest args)]))
