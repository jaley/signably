(ns signably.db
  "Storage backend for card state")

(defprotocol StoreReader
  (get-card-state [store card-id] "Retrieve persisted state for given card"))

;; TODO: add card state updates
(defprotocol StoreWriter
  (create-card [store user-id metadata] "Create a new empty card for this user"))

(defn latest-entry
  "Get the latest entry from the sequentially keyed state map"
  [state]
  (get state (-> state count dec)))

(defrecord InMemoryStore [state]
  StoreReader
  (get-card-state [store id]
    (get @state id))

  StoreWriter
  (create-card [store user-id metadata]
    (-> state
        (swap! (fn [cards]
                 (let [next-id (count cards)]
                   (assoc cards next-id
                          {:id next-id
                           :user user-id
                           :metadata metadata}))))
        latest-entry)))

(defn mem-store
  "Create a new, empty, in-memory store for testing purposes"
  []
  (InMemoryStore. (atom {})))
