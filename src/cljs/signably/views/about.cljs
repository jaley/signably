(ns signably.views.about)

(defn init
  "Returns a factory for the about page view"
  []
  (fn []
    [:span.main
     [:h1 "About signably"]
     [:p "Some interesting things here"]]))
