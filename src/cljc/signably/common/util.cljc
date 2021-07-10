(ns signably.common.util
  "Common helper functions for client and server")

(defn channel-name
  "Return the string channel identifier for a given card-id"
  [card-id]
  (str "signatures:card-" card-id))
