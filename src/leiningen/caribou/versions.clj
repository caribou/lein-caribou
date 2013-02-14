(ns leiningen.caribou.versions
  (:require [leiningen.core.project :as project]
            [clojure.string :as string]
            [clojure.xml :as xml]))

(def all-jar-versions (atom '()))

(defn- dbg
  [& args]
  (let [end (last args)]
    (doseq [a args] (println a))
    end))

(defn maybe-load
  [file]
  (try
    (project/read file)
    (catch Exception e nil)))

(defn project-version
  [map symbol]
  (let [spec (filter #(= (first %) symbol) (:dependencies map))]
    (second (first spec))))

(defn !=
  [& args]
  (not (apply = args)))

(defn parse-version
  [v]
  (map
   #(if (re-find #"[^0-9]" %)
      %
      (Integer. %))
   (string/split v #"\.")))

(defn compare-versions
  [a b]
  (cond
   (and (empty? a) (empty? b)) 0
   (empty? a) 1 ; sorting in reverse order so largest is at head
   (empty? b) -1
   (= (first a) (first b)) (compare-versions (rest a)
                                             (rest b))
   (or (not (number? (first a)))
       (not (number? (first b)))) (* -1 (compare (str (first a))
                                                 (str (first b))))
       :default (* -1 (compare (first a) (first b)))))

(defn pull-jar-versions
  []
  (let [url "http://clojars.org/repo/all-jars.clj"
        versions-source (str "(" (slurp url) ")")
        all-versions (read-string versions-source)]
    (swap! all-jar-versions (constantly all-versions))))

(defn clojars-greatest
  [name]
  (last (last (filter #(= name (str (first %))) @all-jar-versions))))

(defn local-greatest
  [name]
  (let [env (java.lang.System/getenv)
        home (get env "HOME")
        m2-home (get env "M2_HOME")
        m2-config (and m2-home (xml/parse
                                (str m2-home "/settings.xml")))
        setting (and m2-config
                     (first (:content
                             (first (filter #(= (:tag %) :localRepository)
                                            (:content m2-config))))))
        root-dir (or (and setting
                          (string/replace setting
                                          #"\$\{.*\}"
                                          #(case % "${user.home}" home
                                                 "${env.HOME}" m2-home
                                                 ;; $ is magic here
                                                 (clojure.string/escape
                                                  % {\$ "\\$"}))))
                     (str home "/.m2"))
        repo [root-dir "/repository/"]
        ns-spec (string/split name #"[/.]")
        path (concat repo ns-spec)
        greatest-version (fn [dir]
                           (let [subs (.listFiles dir)
                                 dirs (filter #(.isDirectory %) subs)
                                 strs (map #(.getName %) dirs)
                                 parsed (map parse-version strs)
                                 sorted (sort compare-versions parsed)]
                             (first sorted)))]                             
    (loop [p path pathstr "/"]
      (let [f (java.io.File. pathstr)]
        (if (.exists f)
          (if (empty? p)
            (string/join "." (greatest-version f))
            (recur (rest p) (str pathstr "/" (first p))))
          (throw (Error. (str "invalid path for m2 repository: " pathstr))))))))

(def caribou-jars
  ["antler/caribou-core"
   "antler/caribou-frontend"
   "antler/caribou-api"
   "antler/caribou-admin"
   "antler/lein-caribou"])

(def potential-projects
  ["/project.clj"
   "/site/project.clj"
   "/api/project.clj"
   "/admin/project.clj"])

(defn jar-versions
  [name]
  {:name name
   :local (local-greatest name)
   :clojars (clojars-greatest name)})

(defn current-version
  [name project]
  (project-version project (symbol name)))

(defn check-deps
  [versions path]
  (if-let [project (maybe-load path)]
    (reduce
     #(assoc %1
        (:name %2)
        (project-version project (symbol (:name %2))))
     {:path path} versions)))

(defn versions
  [& args]
  (pull-jar-versions)
  (let [command-line (rest args)
        path (or (and command-line (first command-line)) ".")
        caribou-versions (map jar-versions caribou-jars)
        versions-map (reduce #(assoc %1 (:name %2) (dissoc %2 :name)) {} caribou-versions)
        projects (map #(str path %) potential-projects)
        project-versions (map (partial check-deps caribou-versions) projects)]
    (doseq [version project-versions]
      (if version
        (let [path (:path version)
              version (dissoc version :path)]
          (println "Project versions in" path "--------------------------------------------")
          (doseq [[dep-name number] (seq version)]
            (if number
              (let [version-map (get versions-map dep-name)
                    clojars-version (:clojars version-map)
                    local-version (:local version-map)]
                (println (str "  " dep-name
                              " version in PROJECT: " number
                              " <= LOCALLY: " local-version
                              " <= on CLOJARS: " clojars-version))))))))))
