(ns signably.presence)

(defprotocol User
  (label  [u] "String label for user")
  (color  [u] "String color code assigned to this user"))

(extend-protocol User
  string
  (label [usr] usr)
  (color [usr] "green"))

(defn control
  "Reagent render function for presence component"
  [users]
  [:div.presence
   [:div.presence-title "Online"]
   [:ul.presence-list
    (for [user users]
      [:li {:style {:color (color user)}} (name user)])]])
