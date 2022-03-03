(ns signably.helpers.colors
  "Color generators for strokes")

(def palette
  ["red" "green" "blue" "orange" "purple" "pink" "black"])

(defn color-for-user
  "Return a color code for the given user id"
  [user-id]
  (-> user-id hash (mod (count palette)) palette))
