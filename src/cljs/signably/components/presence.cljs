(ns signably.components.presence
  "Reagent component to show the list of collaborators active")

(defn init
  "Returns a factory fn for the presence bar"
  [users]
  (fn []
    [:div.presence
     [:div.presence-title "Collaborators"]
     [:ul.presence-list
      (for [{:keys [id name color]} @users]
        ^{:key id} [:li {:style {:color color}} name])]]))
