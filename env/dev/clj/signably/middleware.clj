(ns signably.middleware
  (:require
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.params :refer [wrap-params]]
   [prone.middleware :refer [wrap-exceptions]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.format :refer [wrap-restful-format]]))

(def middleware
  (let [defaults (assoc-in site-defaults [:security :anti-forgery] false)]
    [#(wrap-defaults % defaults)
     wrap-restful-format
     wrap-exceptions
     wrap-reload]))
