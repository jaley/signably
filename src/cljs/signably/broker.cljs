(ns signably.broker
  "Client side async message broker component."
  (:require
   [signably.common.messages :as messages]
   [signably.session :as session]
   [signably.pubsub :as ably]
   [clojure.core.async :as async]))

(def ^:const message-batch-duration-ms 50)

(defn- batch-every-ms
  "Batches messages passing through in-ch and delivers a vector
  to returned output channel for all messages that arrived within
  timeout-ms."
  [in-ch timeout-ms]
  (let [out-ch (async/chan)]
    (async/go-loop [buffer  []
                    time-ch (async/timeout timeout-ms)]
      (let [[msg ch] (async/alts! [in-ch time-ch])]
        (cond
          ;; end of batch timer - if we have messages, forward them
          (= ch time-ch)
          (do
            (when (seq buffer)
              (async/>! out-ch buffer))
            (recur [] (async/timeout timeout-ms)))

          ;; input channel has closed, stop looping
          (nil? msg)
          (when (seq buffer)
            (async/>! out-ch buffer))

          ;; single incoming line message, buffer it
          :else
          (recur (conj buffer msg) time-ch))))
    [out-ch]))

(defn- batch-packer
  "Returns a function that packages a vector of Lines into a Batch message"
  [user-id]
  (let [next-id  (atom 0)
        metadata {:color "green"}]
    (fn [lines]
      (let [message-id (messages/message-id (swap! next-id inc))]
        (messages/batch user-id message-id metadata lines)))))

(defn- unbatch
  "Channel is expected to contain batch messages. Return value
  is a channel containing the unbatched versions for rendering."
  [ch]
  (let [out (async/chan)]
    (async/go-loop []
      (when-let [batch (async/<! ch)]
        (doseq [line (:lines batch)]
          (async/>! out line))
        (recur)))
    out))

(defn- dump
  "Helper to log messages from channel"
  [ch]
  (async/go-loop []
    (when-let [msg (async/<! ch)]
      (.log js/console msg)
      (recur))))

(defn init-broker!
  "Connect a new message broker and return the output channel.
  Expectation is that all Line objects reaching the returned
  channel will be rendered to canvas. stroke-ch should be a channel
  providing the raw stroke input data (Lines) from the user"
  [stroke-ch]
  (let [user-id (session/session-id)
        card-id (session/active-card-id)
        stroke-mch (async/mult stroke-ch)
        render-ch (async/chan)
        render-mch (async/mix render-ch)
        [ably-in ably-out] (ably/channels-for-card user-id card-id)]

    ;; tap raw input to batching and publishing through Ably
    (as-> (async/tap stroke-mch (async/chan)) ch
      (batch-every-ms ch message-batch-duration-ms)
      (async/map (batch-packer user-id) ch)
      (async/pipe ch ably-in))

    ;; connect incoming messages from other users to the render loop
    (as-> ably-out ch
      (async/pipe ch (async/chan 1 (remove #(= user-id (:user-id %)))))
      (unbatch ch)
      (async/admix render-mch ch))

    ;; loop input directly back to rendering channel for low latency
    ;; rendering of user direct input (and return it to caller)
    ;; note: need echo disabled on Ably for this
    (async/admix render-mch (async/tap stroke-mch (async/chan)))
    render-ch))
