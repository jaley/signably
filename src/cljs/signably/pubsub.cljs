(ns signably.pubsub
  (:require
   [ajax.core :as ajax]
   [Ably :as ably]))

(defn token-request-url
  "Format a URL for the Ably authUrl param. Used to obtain signed TokenRequests"
  [card-id]
  (str "/api/token/" card-id))


(defn realtime-client
  "Construct and return an Ably Realtime client using token authentication.
  Client will request access to card-id related channels for user-id."
  [user-id card-id]
  (ably/Realtime. #js {:authUrl (token-request-url card-id)
                       :authParams {:user-id user-id}}))
