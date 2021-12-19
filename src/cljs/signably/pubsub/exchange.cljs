(ns signably.pubsub.exchange
  "Broadcast model updates to pubsub listeners, and notify
  local model of changes from remote broadcasts"
  (:require [clojure.core.async :as async]
            [signably.pubsub.ably :as ably]
            [signably.pubsub.debounce :refer [debouncer]]
            [signably.session :as session]))

(defprotocol Inbound
  (update-model [this stroke]
    "Update model state with fresh stroke data"))

(defprotocol Outbound
  (broadcast [this stroke]
    "Share updated stroke data with pubsub listeners"))

(defn init
  "Set up an exchange for model state, such that updates to the model
  are broadcast to this card's channel and other updates are pulled in.
  Given `model` must implement Inbound protocol."
  [model]
  (let [user-id (session/session-id)
        card-id (session/active-card-id)
        [ably-in ably-out] (ably/channels-for-card user-id card-id)
        outbound (debouncer ably-in)
        _ (async/reduce update-model model ably-out)]
    (reify Outbound
      (broadcast [_ stroke]
        (async/put! outbound stroke)))))
