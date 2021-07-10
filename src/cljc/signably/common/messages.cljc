(ns signably.common.messages
  "Message data types shared between CLJ and CLJS")

(defn message-id
  "Construct a message ID map"
  [m-id]
  {:m-id m-id})

(defrecord Line [x1 y1 x2 y2])

(defn line
  "Construct a map to represent a line on the canvas"
  [x1 y1 x2 y2]
  {:x1 x1, :y1 y1,
   :x2 x2, :y2 y2 })

(defn batch
  "Construct a map representing a batch of buffered line maps,
  ready for transmission."
  [user-id message-id metadata lines]
  {:user-id user-id
   :message message-id
   :metadata metadata
   :lines lines})
