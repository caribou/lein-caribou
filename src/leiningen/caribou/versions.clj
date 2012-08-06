(ns leinengen.caribou.versions)

(defn- maybe-slurp
  [file]
  (try (slurp file)
       (catch Exception e "")))

(defn deps
  [file]
  (let [f (maybe-slurp file)
        :admin (current-version f "antler/caribou-admin")
        :api (current-version f

(defn versions
  [& args]
  (let [base (deps "project.clj")
        admin (deps "admin/project.clj")
        api (deps "api/project.clj")
        site (deps "site/project.clj")
        core-version (get-version caribou-core)
        admin-version (get-version caribou-admin)
        api-version (get-version caribou-api)
        frontend-version (get-version caribou-frontend)