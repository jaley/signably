(ns signably.session
  "Utils to set some useful session state through reagent"
  (:require [reagent.session :as session]))

(defn- random-session-id
  "Generate a random base 36 ID for the session"
  []
  (.toString (.floor js/Math (* 1e15 (.random js/Math))) 36))

(defn session-id
  "Return the random ID for the current session"
  []
  (or (session/get ::session-id)
      (let [session-id (random-session-id)]
        (session/put! ::session-id session-id)
        session-id)))

(defn active-card-id
  "Return the card ID set by Accountant when navigating pages"
  []
  (or (session/get-in [:route :route-params :card-id])
      (throw (js/Error. "card-id not set in Reagent session"))))
