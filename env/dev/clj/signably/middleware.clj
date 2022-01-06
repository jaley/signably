(ns signably.middleware
  (:require
   [prone.middleware :refer [wrap-exceptions]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.format :refer [wrap-restful-format]]))

(def middleware
  [#(wrap-defaults % site-defaults)
   wrap-restful-format
   wrap-exceptions])
