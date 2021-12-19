(ns signably.helpers.svg
  "Helper functions to generate SVG elements from data"
  (:require [clojure.string :as str]))

(defn- move-command
  "Format an SVG Path move command (string) to given point"
  [[x y]]
  (str "M " x \space y \space))

(defn- line-command
  "Format an SVG Line command (string) from previous point to given point"
  [[x y]]
  (str "L " x \space y \space))

(defn- path-commands
  "Return the `d` attribute for an SVG Path running through given points"
  [points]
  (str/join \space
            (cons (move-command (first points))
                  (map line-command (rest points)))))

(defn path
  "Return an SVG Path element that runs through given point sequence.
  Points is a seq of [x y] vectors."
  [points {:keys [class fill stroke]
           :or   {class  "signatures"
                  fill   "transparent"
                  stroke "green"}
           :as attrs}]
  [:path
   {:d      (path-commands points)
    :class  class
    :fill   fill
    :stroke stroke}])
