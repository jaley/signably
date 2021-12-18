(ns signably.components.svg-canvas
  (:require [reagent.core :as r]
            [clojure.string :as str]))

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

(defn move-command
  [[x y]]
  (str "M " x \space y \space))

(defn line-command
  [[x y]]
  (str "L " x \space y \space))

(defn path-commands
  [points]
  (str/join \space
            (cons (move-command (first points))
                  (map line-command (rest points)))))

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
         ^{:key id} [:path.signatures
                     {:d (path-commands (:points stroke))
                      :fill "transparent"
                      :stroke "green"}])])))
