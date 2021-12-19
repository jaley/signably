(ns signably.components.svg-canvas
  (:require [reagent.core :as r]
            [signably.helpers.svg :as svg]))

(defn- point-transform
  "Returns a function that will take mouse events and transform
  them to [x y] points relative to given element ID"
  [elem-id]
  (fn [^MouseEvent e]
    (let [elem (.getElementById js/document elem-id)
          rect (.getBoundingClientRect elem)]
      [(- (.-clientX e) (.-left rect))
       (- (.-clientY e) (.-top rect))])))

(defn- new-stroke-id
  []
  (-> (rand 1E12) int))

(defn- new-stroke
  [starting-point]
  {:id     (new-stroke-id)
   :points [starting-point]})

(defn- add-point
  [strokes stroke-id point]
  (update-in strokes [stroke-id :points] conj point))

(defn init
  "Return a factory to produce an SVG-based drawing canvas"
  [id w h]
  (let [strokes     (r/atom {})
        open-stroke (atom nil)
        get-point   (point-transform id)]
    (fn []
      [:svg
       {:id id
        :width w
        :height h
        :on-mouse-down (fn [e]
                         (let [stroke (-> e get-point new-stroke)]
                           (r/rswap! strokes assoc (:id stroke) stroke)
                           (reset! open-stroke (:id stroke))))
        :on-mouse-move (fn [e]
                         (when @open-stroke
                           (r/rswap! strokes add-point @open-stroke (get-point e))))
        :on-mouse-up (fn [e]
                       (when @open-stroke
                         (r/rswap! strokes add-point @open-stroke (get-point e))
                         (reset! open-stroke nil)))}
       [:rect.paper {:width "100%", :height "100%"}]

       (for [[id stroke] @strokes]
         ;; TODO: add user-based color as stroke attribute
         (with-meta
           (svg/path (:points stroke) :class "signatures")
           {:key id}))])))
