(ns signably.pubsub
  "Ably helpers to connect pub/sub messages to ClojureScript async"
  (:require
   [signably.common.util :as util]
   [signably.common.messages :as msg]
   [clojure.core.async :as async]
   [ajax.core :as ajax]
   [cognitect.transit :as transit]
   [Ably :as ably]))

(defn- transit-serialise
  "Convert a Clojure map to a string for transmission"
  [m]
  (let [w (transit/writer :json)]
    (transit/write w m)))

(defn- token-request-url
  "Format a URL for the Ably authUrl param. Used to obtain signed TokenRequests"
  [card-id]
  (str "/api/token/" card-id))

(defn- realtime-client
  "Construct and return an Ably Realtime client using token authentication.
  Client will request access to card-id related channels for user-id."
  [user-id card-id]
  (ably/Realtime. #js {:authUrl (token-request-url card-id)
                       :authParams {:user-id user-id}}))

(defn- attach-publisher!
  "Set up an async publisher worker to push all messages coming through ch
  to Ably for other clients to render them."
  [client ch channel-name]
  (let [ably-chan (.. client -channels (get channel-name))]
    (.log js/console ably-chan)
    (async/go-loop []
      (when-let [msg (async/<! ch)]
        (.log js/console msg)
        (.publish ably-chan "batch" (transit-serialise msg))
        (recur)))))

(defn- attach-subscriber!
  "Set up an async subscriber to receive incoming messages from other clients
  and push them to ch for the rendering loop to unpack."
  [client ch channel-name])

(defn channels-for-card
  "Returns a vector of [in out] async channels for messages going in
  and coming out of the channel for given card-id"
  [user-id card-id]
  (let [in-ch  (async/chan)
        out-ch (async/chan)
        client (realtime-client user-id card-id)]
    (attach-publisher! client in-ch (util/channel-name card-id))
    (attach-subscriber! client out-ch (util/channel-name card-id))
    [in-ch out-ch]))
