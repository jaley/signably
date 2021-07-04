(ns signably.ably
  "Functions for working with Ably Java SDK"
  (:require
   [config.core :refer [env]]
   [cheshire.core :as json])
  (:import
   [io.ably.lib.rest AblyRest Auth$TokenParams]
   [io.ably.lib.types Param]))

(defn- ^AblyRest rest-client
  "Construct a new AblyRest client using environment-injected private key"
  []
  (AblyRest. (env :ably-private-key)))

(defmacro ^:private pojo
  "Generate code to assign params using public mutable fields in pojo"
  [pojo-class & {:as params}]
  (let [inst (gensym)]
    `(let [~inst (new ~pojo-class)]
       ~@(for [[param# value#] params]
           `(set! (. ~inst ~(symbol param#)) ~value#))
       ~inst)))

(defn- extract-params
  [param-map]
  (reduce-kv
   (fn [m k ^Param v]
     (assoc m k (.value v)))
   {}
   (into {} param-map)))

(defn generate-token-for-client
  "Generate an Ably access token for client with given client-id"
  [client-id]
  (let [token-params (pojo Auth$TokenParams :clientId client-id)]
    (-> (rest-client)
        .auth
        (.createTokenRequest token-params nil)
        ;; TODO: Feels a bit gross to dump and reparse JSON, but
        ;; this at least ensures fields needed by client will be
        ;; correct as SDK evolves
        .asJson
        (json/parse-string keyword))))
