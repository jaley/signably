(ns signably.server
    (:require
     [signably.routes :refer [build-routes]]
     [signably.db :as db]
     [config.core :refer [env]]
     [ring.adapter.jetty :refer [run-jetty]])
    (:gen-class))

(defn -main
  "Backend server process entry point"
  [& args]
  (let [port (or (env :port) 3000)
        store (db/mem-store)]
    (run-jetty (build-routes store) {:port port :join? false})))
