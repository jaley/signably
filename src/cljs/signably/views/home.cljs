(ns signably.views.home
  "Home landing page view"
  (:require
   [signably.router :refer [path-for]]
   [accountant.core :as accountant]
   [ajax.core :as ajax]))

(defn new-card!
  "Click hanlder fn for new card creation"
  []
  (ajax/POST "/api/card"
        {:params {:message "message goes here"
                  :user-id "abcd-1234"}
         :handler (fn [{:keys [card ably-token-request] :as response}]
                    (.log js/console (clj->js ably-token-request))
                    (accountant/navigate! (path-for :card {:card-id (:id card)})))
         :error-handler (fn [response]
                          (.log js/console "New card error: " response))}))

(defn init
  "Returns a factory for the home landing page view"
  []
  (fn []
    [:div.section
     [:div
      [:button {:on-click new-card!}
       "New Card"]]
     [:div
      [:h4 "Open Cards"]
      [:p "You have no cards open."]]]))
