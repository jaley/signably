(ns signably.pubsub.ably
  "Ably helpers to connect pub/sub messages to ClojureScript async"
  (:require
   [signably.common.util :as util]
   [clojure.core.async :as async]
   [ajax.core :as ajax]
   [cognitect.transit :as transit]
   [Ably :as ably]))

(defn- transit-serialise
  "Convert a Clojure map to a string for transmission"
  [m]
  (let [w (transit/writer :json)]
    (transit/write w m)))

(defn- transit-deserialise
  "Convert incoming string message payloads to Clojure maps"
  [s]
  (let [r (transit/reader :json)]
    (transit/read r s)))

(defn- token-request-url
  "Format a URL for the Ably authUrl param. Used to obtain signed TokenRequests"
  [card-id]
  (str "/api/token/" card-id))

(defn- realtime-client
  "Construct and return an Ably Realtime client using token authentication.
  Client will request access to card-id related channels for user-id."
  [user-id card-id]
  (ably/Realtime. #js {:authUrl (token-request-url card-id)
                       :clientId user-id
                       :echoMessages false}))

(defn- attach-publisher!
  "Set up an async publisher worker to push all messages coming through ch
  to Ably for other clients to render them."
  [client ch channel-name]
  (let [ably-chan (.. client -channels (get channel-name))]
    (async/go-loop []
      (when-let [msg (async/<! ch)]
        (.publish ably-chan "batch" (transit-serialise msg))
        (recur)))))

(defn- attach-subscriber!
  "Set up an async subscriber to receive incoming messages from other clients
  and push them to ch for the rendering loop to unpack."
  [client ch channel-name]
  (let [ably-chan (.. client -channels (get channel-name))]
    (.subscribe ably-chan "batch"
                (fn [msg]
                  (when-let [msg (transit-deserialise (.-data msg))]
                    (async/put! ch msg))))))

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

(defn listen-for-presence
  "Attaches the given listener functions to the Ably presence
  callbacks on the appropriate channel for this card"
  [user-id card-id on-enter on-leave on-start]
  (let [client (realtime-client user-id card-id)
        chan   (.. client -channels (get (util/channel-name card-id)))]
    (.. chan -presence (subscribe "enter" on-enter))
    (.. chan -presence (subscribe "leave" on-leave))
    (.. chan -presence (get on-start))
    (.. chan -presence enter)))
