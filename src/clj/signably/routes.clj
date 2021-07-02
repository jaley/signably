(ns signably.routes
  (:require
   [signably.spa :as spa]
   [signably.service :as service]
   [signably.middleware :refer [middleware]]
   [reitit.ring :as reitit-ring]))

(defn build-routes
  "Builds all ring handlers for the service API and SPA."
  [store]
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/" {:get {:handler spa/render-spa}}]])
   (reitit-ring/routes
    (service/api-routes store)
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    spa/render-spa) ; default route: render SPA and let it check client side
   {:middleware middleware}))
