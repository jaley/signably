(ns signably.views.card
  "Main card signing view"
  (:require
   [signably.components.presence :as presence]
   [signably.components.svg-canvas :as canvas]))

(defn init
  "Returns a factory for the card page"
  []
  (fn []
    [:div.section
     [presence/init ["James" "Bob"]]
     [canvas/init "signing-canvas" 1080 768]]))
