(ns signably.views.card
  "Main card signing view"
  (:require
   [signably.components.presence :as presence]
   [signably.components.svg-canvas :as canvas]
   [signably.models.collaborators :as c-model]
   [signably.models.signatures :as s-model]
   [signably.session :as session]))

(defn init
  "Returns a factory for the card page"
  []
  (let [collabs (c-model/init (session/session-id)
                              (session/active-card-id))
        sigs    (s-model/init (session/active-card-id))]
    (fn []
      [:div.section
       [presence/init collabs]
       [canvas/init "signing-canvas" 1080 768 sigs sigs]])))
