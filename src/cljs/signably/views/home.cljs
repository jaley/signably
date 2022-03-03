(ns signably.views.home
  "Home landing page view"
  (:require
   [signably.router :refer [path-for]]
   [signably.session :refer [session-id]]
   [signably.helpers.xsrf :as xsrf]
   [accountant.core :as accountant]
   [ajax.core :as ajax]))



(defn new-card!
  "Click hanlder fn for new card creation"
  []
  (ajax/POST "/api/card"
             {:headers {:X-XSRF-Token (xsrf/get-token)}
              :params {:message (.. js/document
                                    (getElementById "new-card-message")
                                    -value)
                       ;; TODO: temporarily using session IDs as users
                       :user-id (session-id)}

              :handler
              (fn [{:keys [card] :as response}]
                (accountant/navigate!
                 (path-for :card {:card-id (:id card)})))

              :error-handler
              (fn [response]
                (.log js/console "New card error: " response))}))

(defn init
  "Returns a factory for the home landing page view"
  []
  (fn []
    [:div.section
     [:div
      [:input.message-input
       {:id "new-card-message"
        :type "text"
        :defaultValue "Happy Birthday!"}]
      [:button {:on-click new-card!} "New Card"]]
     [:div
      [:h4 "Open Cards"]
      [:p "You have no cards open."]]]))
