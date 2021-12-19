(ns signably.views.card
  "Main card signing view"
  (:require
   [signably.components.presence :as presence]
   [signably.components.svg-canvas :as canvas]
   [signably.models.signatures :as model]
   [signably.session :as session]))

(defn init
  "Returns a factory for the card page"
  []
  (let [model (model/init (session/active-card-id))]
    (fn []
      [:div.section
       [presence/init ["James" "Bob"]]
       [canvas/init "signing-canvas" 1080 768 model model]])))
