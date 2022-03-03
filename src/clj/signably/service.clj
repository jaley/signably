(ns signably.service
  "Routes and handler functions for the service API"
  (:require
   [signably.db :as db]
   [signably.ably :as ably]
   [reitit.ring :as reitit-ring]))

(defn- get-card-id
  "Helper to retrieve and cast card-id in params map"
  [req]
  (-> req
      (get-in [:path-params :card-id])
      Integer/parseUnsignedInt))

(defn- new-card-handler
  "Create a request handler for creating new cards"
  [store]
  (fn [req]
    ;; TODO: User ID should NOT be a client parameter!
    (let [user (get-in req [:params :user-id])
          msg  (get-in req [:params :message] "")
          card (db/create-card store user {:message msg})]
      (if card
        {:status 200
         :body {:card card}}
        {:status 400
         :body {:message "Error creating card"}}))))

(defn- load-card-handler
  "Create a handler function for loading card state from storage"
  [store]
  (fn [req]
    (let [id (get-card-id req)]
      (if-let [card (db/get-card-state store id)]
        {:status 200
         :body {:card card}}
        {:status 404
         :body {:message "Not found" :id id}}))))

(defn- token-request-handler
  "Create a ring handler function for token requests"
  [store]
  (fn [req]
    (let [user (get-in req [:params :clientId])
          card (get-card-id req)]
      {:status 200
       :body   (ably/generate-token-for-client user card)})))

(defn api-routes
  "Returns a Ring handler for the /api service endpoints"
  [store]
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/api"
      ["/card"
       ["" {:post {:handler (new-card-handler store)}}]
       ["/:card-id" {:get {:handler (load-card-handler store)}}]]
      ["/token/:card-id" {:get {:handler (token-request-handler store)}}]]])))
