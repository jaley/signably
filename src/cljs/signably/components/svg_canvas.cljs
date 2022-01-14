(ns signably.components.svg-canvas
  (:require [reagent.core :as r]
            [signably.common.data :as data]
            [signably.models.signatures :as model]
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

(defn init
  "Return a factory to produce an SVG-based drawing canvas"
  [id w h stroke-reader stroke-writer]
  (let [open-stroke-id (atom nil)
        get-point      (point-transform id)]
    (fn []
      [:svg
       {:id id
        :width w
        :height h
        :on-mouse-down (fn [e]
                         (reset!
                          open-stroke-id
                          (model/begin stroke-writer (get-point e))))
        :on-mouse-move (fn [e]
                         (when-let [id @open-stroke-id]
                           (model/append stroke-writer
                                         @open-stroke-id (get-point e))))
        :on-mouse-up (fn [e]
                       (when-let [id @open-stroke-id]
                         (model/append stroke-writer
                                       @open-stroke-id (get-point e))
                         (reset! open-stroke-id nil)))}

       ;; paper background
       [:rect.paper {:width "100%", :height "100%"}]

       ;; add a path for each stroke in the model
       (for [[id stroke] (model/read stroke-reader)
             :let [points (::data/points stroke)]]
         ;; TODO: add user-based color as stroke attribute
         ^{:key id} [svg/path points {:class "signatures"}])])))
