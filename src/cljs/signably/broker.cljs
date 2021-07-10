(ns signably.broker
  "Client side async message broker component."
  (:require
   [signably.common.messages :as messages]
   [signably.session :as session]
   [signably.pubsub :as ably]
   [clojure.core.async :as async]))

(def ^:const message-batch-duration-ms 100)

(defn- batch-every-ms
  "Batches messages passing through in-ch and delivers a vector
  to returned output channel for all messages that arrived within
  timeout-ms."
  [timeout-ms in-ch]
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
      (let [message-id (messages/MessageId. (swap! next-id inc))]
        (messages/Batch. user-id message-id metadata lines)))))

(defn- dump
  "Helper to log messages from channel"
  [ch]
  (async/go-loop []
    (.log js/console (async/<! ch))
    (recur)))

(defn init-broker!
  "Connect a new message broker and return the output channel.
  Expectation is that all Line objects reaching the returned
  channel will be rendered to canvas. stroke-ch should be a channel
  providing the raw stroke input data (Lines) from the user"
  [stroke-ch]
  (let [user-id    (session/session-id)
        card-id    (session/active-card-id)
        stroke-mch (async/mult stroke-ch)
        render-ch  (async/chan)
        ably       (ably/realtime-client user-id card-id)]
    (.log js/console ably)
    ;; tap raw input to batching and publishing through Ably
    (->> (async/tap stroke-mch (async/chan))
         (batch-every-ms message-batch-duration-ms)
         (async/map (batch-packer user-id))
         dump)

    ;; loop input directly back to rendering channel for low latency
    ;; rendering of user direct input (and return it to caller)
    ;; note: need echo disabled on Ably for this
    (async/pipe (async/tap stroke-mch (async/chan)) render-ch)))
