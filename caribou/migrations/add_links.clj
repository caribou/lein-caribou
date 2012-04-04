(require '[caribou.model :as model])

(defn build-links []
  (model/invoke-models)
  (model/update :model ((model/models :site) :id)
          {:fields [{:name "Domains"
                     :type "collection"
                     :target_id ((model/models :domain) :id)}
                    {:name "Pages"
                     :type "collection"
                     :target_id ((model/models :page) :id)}]}
          {:op :migration}))

(defn migrate
  []
  (build-links))

(migrate)

