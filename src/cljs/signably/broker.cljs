(ns signably.broker
  "Client side async message broker component."
  (:require
   [clojure.core.async :as async]))


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
          (= ch time-ch) ; end of batch timer
          (do
            (async/>! out-ch buffer)
            (recur [] (async/timeout timeout-ms)))

          (nil? msg) ; in-ch closed
          (when (seq buffer)
            (async/>! out-ch buffer))

          :else
          (recur (conj buffer msg) time-ch))))
    out-ch))

(defn init-broker!
  "Connect a new message broker and return the output channel.
  Expectation is that all Line objects reaching the returned
  channel will be rendered to canvas. stroke-ch should be a channel
  providing the raw stroke input data (Lines) from the user"
  [stroke-ch]
  (let [render-ch (async/chan)]
    ;; TODO: Connect to Ably and batching. Loopback for render only now
    (async/pipe stroke-ch render-ch)))
