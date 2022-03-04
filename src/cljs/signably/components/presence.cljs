(ns signably.components.presence
  "Reagent component to show the list of collaborators active"
  (:require [goog.string :as gstring]
            [goog.string.format]))

(defn fmt-counter
  "Zero-padded string for counter value"
  [n]
  (gstring/padNumber n 3))

(defn init
  "Returns a factory fn for the presence bar"
  [users]
  (fn []
    [:div.presence
     [:div.presence-title "Collaborators"
      [:div.presence-counter (-> @users count fmt-counter)]]
     [:div.presence-list-container
      [:ul.presence-list
       (for [{:keys [id name color]} @users]
         ^{:key id} [:li {:style {:color color}} name])]]]))
