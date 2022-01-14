(ns signably.common.data
  "Common data type specs and functions"
  (:require [clojure.spec.alpha :as s]))

(s/def ::stroke-id string?)
(s/def ::point (s/tuple int? int?))
(s/def ::points (s/coll-of ::point))
(s/def ::stroke
  (s/keys
   :req [::stroke-id ::points]))

(defn gen-uuid
  "Generate a random UUID using platform APIs and return as string"
  []
  (str
   #?(:clj  (java.util.UUID/randomUUID)
      :cljs (random-uuid))))

(defn new-stroke
  "Create an empty new stroke entity map"
  ([] (new-stroke nil))
  ([point]
   {::stroke-id (gen-uuid)
    ::points    (if point [point] [])}))

(s/fdef new-stroke
  :args (s/cat
         :point (s/nilable ::point))
  :ret  ::stroke)

(defn add-point
  "Add a new point [x, y] to the stroke"
  [stroke point]
  (update-in stroke [::points] conj point))

(s/fdef add-point
  :args (s/cat
         :stroke ::stroke
         :point  ::point)
  :ret  ::stroke
  :fn   (s/and
         #(= (-> % :ret ::points last)
             (-> % :args :point))
         #(= (-> % :ret ::points count)
             (-> % :args :stroke ::points count inc))))
