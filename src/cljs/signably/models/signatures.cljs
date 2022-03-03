(ns signably.models.signatures
  "Model data structure and protocols for card signatures"
  (:require [reagent.core :as r]
            [signably.common.data :as data]
            [signably.session :as session]
            [signably.pubsub.exchange :as exch]))

(defprotocol SignaturesReader
  (read [this]
    "Returns the current model snapshot: map[id -> stroke]"))

(defprotocol SignaturesWriter
  (begin  [this point]
    "Begin writing a new stroke, returns new stroke ID")
  (append [this stroke-id point]
    "Appends a given point to the signature model for given stroke-id"))

(defn init
  "Construct and return a model implementation for given card-id"
  [card-id]
  (let [user-id    (session/session-id)
        signatures (r/atom {})
        exchange   (exch/init
                    (reify exch/Inbound
                      (update-model [model stroke]
                        (let [{::data/keys [stroke-id]} stroke]
                          (r/rswap! signatures assoc stroke-id stroke))
                        model)))]
    (reify
      SignaturesReader
      (read [this] @signatures)

      SignaturesWriter
      (begin [this point]
        (let [{::data/keys [stroke-id] :as stroke}
              (data/new-stroke user-id point)]
          (r/rswap! signatures assoc stroke-id stroke)
          (exch/broadcast exchange stroke)
          stroke-id))
      (append [this stroke-id point]
        (r/rswap! signatures update-in [stroke-id] data/add-point point)
        (exch/broadcast exchange (@signatures stroke-id))))))
