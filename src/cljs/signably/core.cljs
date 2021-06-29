(ns signably.core
  (:require
   [signably.presence :as presence]
   [signably.canvas :as canvas]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]
   [ajax.core :refer [GET POST]]))

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :home]
    ["/card/:card-id"
     ["" :card]
     ["/zoom" :zoom]]
    ["/about" :about]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

;; -------------------------
;; Page components

(defn nav-component
  []
  [:div#nav.nav-bar
   [:div.nav-title "Signably"]
   [:div.nav-links
    [:p
     [:a {:href (path-for :home)} "Home"]
     " | "
     [:a {:href (path-for :about)} "About Signably"]]]])

(defn new-card!
  []
  (POST "/api/card"
        {:params {:message "message goes here"
                  :user-id "abcd-1234"}
         :handler (fn [{:keys [id] :as response}]
                    (accountant/navigate! (path-for :card {:card-id id}))                    )
         :error-handler (fn [response]
                          (.log js/console "New card error: " response))}))

(defn home-page
  []
  (fn []
    [:div.section
     [:div
      [:button {:on-click new-card!}
       "New Card"]]
     [:div
      [:h4 "Open Cards"]
      [:p "You have no cards open."]]]))


(defn about-page
  []
  (fn []
    [:span.main
     [:h1 "About signably"]]))

(defn card-page
  []
  (fn []
    [:div.section
     [presence/control ["James" "Bob"]]
     [canvas/component "signing-canvas" 1080 768]]))

;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :home #'home-page
    :about #'about-page
    :card #'card-page))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [nav-component]
       [page]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [current-page] (.getElementById js/document "app")))

(defn init! []
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