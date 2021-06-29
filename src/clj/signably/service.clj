(ns signably.service
  "Routes and handler functions for the service API"
  (:require
   [signably.db :as db]
   [reitit.ring :as reitit-ring]))

(defn- new-card-handler
  "Create a request handler for creating new cards"
  [store]
  (fn [req]
    ;; TODO: User ID should NOT be a client parameter!
    (let [user (get-in req [:params :user-id])
          msg  (get-in req [:params :message] "")]
      (if-let [card (db/create-card store user {:message msg})]
        {:status 200
         :body card}
        {:status 400
         :body {:message "Error creating card"}}))))

(defn- load-card-handler
  "Create a handler function for loading card state from storage"
  [store]
  (fn [req]
    (let [id (get-in req :path-params :card-id)]
      (if-let [card (db/get-card-state store id)]
        {:status 200
         :body {:card card}}
        {:status 404
         :body {:message "Not found"}}))))

(defn api-routes
  [store]
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/api"
      ["/card"
       ["" {:post {:handler (new-card-handler store)}}]
       ["/:card-id" {:get {:handler (load-card-handler store)}}]]]])))
