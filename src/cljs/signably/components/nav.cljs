(ns signably.components.nav
  "Nav bar component"
  (:require
   [signably.router :refer [path-for]]))

(defn init
  "Returns a factory for the nav component"
  []
  (fn []
    [:div#nav.nav-bar
     [:div.nav-title "Signably"]
     [:div.nav-links
      [:p
       [:a {:href (path-for :home)} "Home"]
       " | "
       [:a {:href (path-for :about)} "About Signably"]]]]))
