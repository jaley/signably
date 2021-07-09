(ns signably.common.messages
  "Message data types shared between CLJ and CLJS")

(defrecord MessageId [session message])

(defrecord Line [x1 y1 x2 y2])

(defrecord Batch [user-id message-id metadata lines])
