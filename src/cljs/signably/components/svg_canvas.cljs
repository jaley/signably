(ns signably.components.svg-canvas
  (:require [reagent.core :as r]
            [ajax.core :as ajax]
            [signably.common.data :as data]
            [signably.models.signatures :as model]
            [signably.helpers.svg :as svg]
            [signably.helpers.colors :as colors]
            [signably.session :as session]))

(defn get-card-info!
  "Load card info and set it in card state reagent atom"
  [card-id card-state]
  (ajax/GET (str"/api/card/" card-id)
            {:handler
             (fn [{:keys [card] :as response}]
               (r/rswap! card-state (constantly card)))

             :error-handler
             (fn [response]
               (.log js/console
                     "Error retrieving card state: " response))}))

(defn- point-transform
  "Returns a function that will take mouse events and transform
  them to [x y] points relative to given element ID"
  [elem-id]
  (fn [^MouseEvent e]
    (let [elem (.getElementById js/document elem-id)
          rect (.getBoundingClientRect elem)]
      [(- (.-clientX e) (.-left rect))
       (- (.-clientY e) (.-top rect))])))

(defn message-elem
  "Reagent component for message text"
  [w h]
  (let [card-state (r/atom nil)]
    (fn []
      (get-card-info! (session/active-card-id) card-state)
      [:text.message-text
       {:x (/ w 2) :y (/ h 2) :text-anchor "middle"}
       (get-in @card-state [:metadata :message])])))

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

       ;; greeting message
       [message-elem w h]

       ;; add a path for each stroke in the model
       (for [[id stroke] (model/read stroke-reader)
             :let [points  (::data/points stroke)
                   user-id (::data/user-id stroke)]]
         ^{:key id} [svg/path points {:class "signatures"
                                      :stroke (colors/color-for-user user-id)}])])))
