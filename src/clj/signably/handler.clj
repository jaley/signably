(ns signably.handler
  (:require
   [signably.db :as db]
   [signably.middleware :refer [middleware]]
   [reitit.ring :as reitit-ring]
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]))

(def mount-target
  [:div#app
   [:h2 "Welcome to signably"]
   [:p "Loading..."]])

(defn head
  "HTML <head> for SPA body"
  []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page
  "SPA Body elements"
  []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "/js/app.js")
    [:script "signably.core.init_BANG_()"]]))


(defn index-handler
  "Ring Handler for main SPA landing page"
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

(defn new-card-handler
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

(defn load-card-handler
  "Create a handler function for loading card state from storage"
  [store]
  (fn [req]
    (let [id (get-in req :path-params :card-id)]
      (if-let [card (db/get-card-state store id)]
        {:status 200
         :body {:card card}}
        {:status 404
         :body {:message "Not found"}}))))

(defn api
  [store]
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/api"
      ["/card"
       ["" {:post {:handler (new-card-handler store)}}]
       ["/:card-id" {:get {:handler (load-card-handler store)}}]]]])))

(defn app
  [store]
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/" {:get {:handler index-handler}}]])
   (reitit-ring/routes
    (api store)
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware middleware}))
