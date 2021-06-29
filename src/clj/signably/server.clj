(ns signably.server
    (:require
     [signably.handler :refer [app]]
     [signably.db :as db]
     [config.core :refer [env]]
     [ring.adapter.jetty :refer [run-jetty]])
    (:gen-class))

(defn -main [& args]
  (let [port (or (env :port) 3000)
        store (db/mem-store)]
    (run-jetty (app store) {:port port :join? false})))
