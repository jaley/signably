(ns signably.models.signatures
  "Model data structure and protocols for card signatures"
  (:require [reagent.core :as r]))

(defprotocol SignaturesReader
  (read [this]
    "Returns the current model snapshot: map[id -> stroke]"))

(defprotocol SignaturesWriter
  (begin  [this point]
    "Begin writing a new stroke, returns new stroke ID")
  (append [this stroke-id point]
    "Appends a given point to the signature model for given stroke-id"))

(defn- new-stroke-id
  "Generate a random Stroke ID"
  []
  ;; TODO: pull in some uuid lib for this
  (-> (rand 1E12) int))

(defn- new-stroke
  "Return a map representing a new stroke starting at given point"
  [id starting-point]
  {:id     id
   :points [starting-point]})

(defn- add-point
  "Update the strokes map with the new point for given stroke-id"
  [strokes stroke-id point]
  (update-in strokes [stroke-id :points] conj point))

(defrecord SignatureMap [signatures]
  SignaturesReader
  (read [this] @signatures)

  SignaturesWriter
  (begin  [this point]
    (let [id (new-stroke-id)]
      (r/rswap! signatures assoc id (new-stroke id point))
      id))
  (append [this stroke-id point]
    (r/rswap! signatures add-point stroke-id point)))

(defn init
  "Set up a signature model for given card ID"
  [card-id]
  (SignatureMap. (r/atom {})))
