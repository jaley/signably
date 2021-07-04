(ns signably.components.canvas)

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

(defrecord StrokeOnCanvas [ctx]
  Stroke
  (next-point [s e]
    (let [[nx ny] (get-point e)]
      (StrokeOnCanvas.
       (doto ctx
         (.lineTo nx ny)
         .stroke
         (.moveTo nx ny)))))
  (complete [s e]
    (let [[x y] (get-point e)]
      (.closePath
       (doto ctx (.lineTo x y) .stroke)))))

(defn- begin-stroke
  "Return a StrokeOnCanvas handler starting at given mouse event"
  [^MouseEvent e]
  (let [ctx   (.. e -target (getContext "2d"))
        [x y] (get-point e)]
    (set! (.-strokeStyle ctx) "green")
    (set! (.-lineWidth ctx) 10)
    (StrokeOnCanvas.
     (doto ctx .beginPath (.moveTo x y)))))


(defn init
  "Return a factory to produce the canvas element"
  [id w h]
  (let [open-stroke (atom nil)]
    (fn []
      [:canvas
       {:id id
        :width w
        :height h
        :on-mouse-down #(reset! open-stroke (begin-stroke %))
        :on-mouse-move #(swap! open-stroke next-point %)
        :on-mouse-up   #(swap! open-stroke complete %)}])))
