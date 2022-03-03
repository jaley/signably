(ns signably.db
  "Storage backend for card state")

(defprotocol StoreReader
  (get-card-state [store card-id] "Retrieve persisted state for given card"))

(defprotocol StoreWriter
  (create-card [store user-id metadata] "Create a new empty card for this user"))

(defn new-card-id
  "Generate a random base 36 ID for a new card"
  []
  (-> (Math/pow 36 10) rand bigint .toBigInteger (.toString 36)))

(defrecord InMemoryStore [state]
  StoreReader
  (get-card-state [store id]
    (get @state id))

  StoreWriter
  (create-card [store user-id metadata]
    (let [id   (new-card-id)
          card {:id id
                :user user-id
                :metadata metadata}]
      (swap! state assoc id card)
      card)))

(defn mem-store
  "Create a new, empty, in-memory store for testing purposes"
  []
  (InMemoryStore. (atom {})))
