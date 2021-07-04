(ns signably.router
  "SPA route definitions and helpers"
  (:require
   [reitit.frontend :as reitit]))

;; SPA client-side route definitions
(def router
  (reitit/router
   [["/" :home]
    ["/card/:card-id"
     ["" :card]]
    ["/about" :about]]))

(defn path-for
  "Lookup relative URL for given page alias (keyword)"
  [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))
