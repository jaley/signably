(ns signably.middleware
  (:require
   [prone.middleware :refer [wrap-exceptions]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.defaults :refer [wrap-defaults]]
   [ring.middleware.format :refer [wrap-restful-format]]))

(def middleware
  [#(wrap-defaults % defaults)
   wrap-restful-format
   wrap-exceptions
   wrap-reload])
