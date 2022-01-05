(ns signably.core
  "SPA main entry point"
  (:require
   [signably.components.nav :as nav]
   [signably.views.home :as home]
   [signably.views.card :as card]
   [signably.views.about :as about]
   [signably.router :refer [router]]
   [reagent.core :as reagent]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]))

(defn current-page
  "Returns a factory to render current page (as set in session),
  with nav header"
  []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [nav/init]
       [page]])))

(defn mount-root
  "Attach to root element in dom. Backend should serve a #app div."
  []
  (rdom/render
   [current-page]
   (.getElementById js/document "app")))

(defn page-for
  "Returns the factory function (var) for the given page alias (keyword)"
  [route]
  (case route
    :home #'home/init
    :about #'about/init
    :card #'card/init))

(defn ^:export init!
  "Entry point, called by script tag in page served by backend"
  []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
