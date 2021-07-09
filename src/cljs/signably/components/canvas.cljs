(ns signably.components.canvas
  (:require
   [clojure.core.async :as async]
   [signably.broker :as broker]
   [signably.common.messages :as messages]))

(defprotocol Stroke
  (next-point [s e] "Process next movement event")
  (complete   [s e] "Process final mouse-up event"))

;; implement for nil to discard unwanted events
(extend-protocol Stroke
  nil
  (next-point [s e] nil)
  (complete   [s e] nil))

(defn- get-point
  "Return a vector with [x y] coordinates for the given event,
  relative to the canvas origin."
  [^MouseEvent e]
  (let [rect (.. e -target getBoundingClientRect)]
    [(- (.-clientX e) (.-left rect))
     (- (.-clientY e) (.-top rect))]))

(defn line-drawer
  "Draw a line on given context 2D. Returns ctx after applying changes."
  [canvas-id]
  (fn [ctx-2d {:keys [x1 y1 x2 y2] :as line}]
    (let [ctx (or ctx-2d
                  (.. js/document (getElementById canvas-id) (getContext "2d")))]
      ;; TODO: pass the color in here somehow
      (set! (.-strokeStyle ctx) "green")
      (set! (.-lineWidth ctx) 10)
      (doto ctx
        .beginPath
        (.moveTo x1 y1)
        (.lineTo x2 y2)
        .stroke
        .closePath))))

(defn renderer
  "Reduces all incoming lines on channel onto the given context"
  [canvas-id line-ch]
  (async/reduce (line-drawer canvas-id) nil line-ch))

(defrecord LocalStroke [ch last-x last-y]
  Stroke
  (next-point [s e]
    (let [[x y] (get-point e)]
      (async/put! ch (messages/Line. last-x last-y x y))
      (LocalStroke. ch x y)))
  (complete [s e]
    (let [[x y] (get-point e)]
      (async/put! ch (messages/Line. last-x last-y x y))
      nil)))

(defn begin-local-stroke
  "Construct a new LocalStroke to track incoming coordinates for mouse events"
  [ch ^MouseEvent e]
  (let [[x y] (get-point e)]
    (LocalStroke. ch x y)))

(defn init
  "Return a factory to produce the canvas element"
  [id w h]
  (let [input-ch (async/chan)
        _ch      (renderer id (broker/init-broker! input-ch))
        stroke   (atom nil)]
    (fn []
      [:canvas
       {:id id
        :width w
        :height h
        :on-mouse-down #(reset! stroke (begin-local-stroke input-ch %))
        :on-mouse-move #(swap! stroke next-point % )
        :on-mouse-up   #(swap! stroke complete %)}])))
