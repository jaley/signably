(ns signably.models.collaborators
  "A model to keep track of active collaborators currently
   signing a card, using Ably Presence"
  (:require [reagent.core :as r]
            [taoensso.timbre :as log]
            [signably.pubsub.ably :as ably]
            [signably.helpers.colors :as colors]))


(defn short-name
  "Returns the last few characters in user-id"
  [user-id]
  (subs user-id (- (count user-id) 4)))

(defn member->collaborator
  "Returns a collaborator map {:name, :color} for
  a member discovered via Ably presence"
  [member]
  {:id   (.-clientId member)
   :name (short-name (.-clientId member))
   :color (colors/color-for-user (.-clientId member))})

(defn init-members
  "Create an initial member list"
  [state err members]
  (if err
    (log/error "Couldn't get member list!" err)
    (r/rswap! state
              (constantly (->> members (map member->collaborator) set)))))

(defn add-member
  "Add a new member to the collaborators list"
  [state member]
  (r/rswap! state conj (member->collaborator member)))

(defn remove-member
  "Remove a member from the list of collaborators"
  [state member]
  (r/rswap! state disj (member->collaborator member)))

(defn init
  "Initialise a new presence model. Returns an observable
  Reagent atom holding the current members. This will be a set
  of {:name, :color} maps"
  [user-id card-id]
  (let [state (r/atom #{})]
    (ably/listen-for-presence
     user-id card-id
     (partial add-member state)
     (partial remove-member state)
     (partial init-members state))
    state))
