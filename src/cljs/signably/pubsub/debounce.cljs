(ns signably.pubsub.debounce
  "Client side async message broker component."
  (:require
   [clojure.core.async :as async]))

(def ^:const default-debounce-interval 250)

(defn debouncer
  "Batches incoming stroke updates to send a message every interval-ms
  so that we're not pushing a message per MouseEvent in the view"
  [out-ch & {:keys [interval-ms]
            :or {interval-ms default-debounce-interval}}]
  (let [in-ch (async/chan)]
    (async/go-loop [cache {}
                    time-ch (async/timeout interval-ms)]
      (let [[msg ch] (async/alts! [in-ch time-ch])]
        (cond
          ;; end of batch timer - if we have messages, forward them
          (= ch time-ch)
          (do
            (doseq [[_ stroke] cache] (async/>! out-ch stroke))
            (recur {} (async/timeout interval-ms)))

          ;; input channel has closed, send cache and stop looping
          (nil? msg)
          (doseq [[_ stroke] cache] (async/>! out-ch stroke))

          ;; single incoming local update, cache it
          :else
          (recur (assoc cache (:id msg) msg) time-ch))))
    in-ch))
