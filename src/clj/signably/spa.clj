(ns signably.spa
  "HTML templates to bootstrap the SPA"
  (:require
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]))

(def ^:private mount-target
  [:div#app
   [:h2 "Welcome to signably"]
   [:p "Loading..."]])

(defn- head
  "HTML <head> for SPA body"
  []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn- loading-page
  "SPA Body elements"
  []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "/js/app.js")
    [:script "signably.core.init_BANG_()"]]))

(defn render-spa
  "Ring Handler to render main SPA landing page"
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})
