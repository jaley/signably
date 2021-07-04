(ns signably.components.presence)

(defprotocol User
  ;; TODO: Need to add an id so that we don't redraw the whole list on updates
  (label  [u] "String label for user")
  (color  [u] "String color code assigned to this user"))

;; TODO: delete string implementation, test only
(extend-protocol User
  string
  (label [usr] usr)
  (color [usr] "green"))

(defn init
  "Returns a factory fn for the presence bar"
  [users]
  (fn []
    [:div.presence
     [:div.presence-title "Active Signees"]
     [:ul.presence-list
      (for [user users]
        [:li {:style {:color (color user)}} (name user)])]]))
