(ns signably.server
  "Backend entry point"
    (:require
     [signably.routes :refer [build-routes]]
     [signably.db :as db]
     [config.core :refer [env]]
     [ring.adapter.jetty :refer [run-jetty]]
     [taoensso.timbre :as log])
    (:gen-class))

(defn -main
  "Backend server process entry point"
  [& args]
  (let [port (or (env :port) 3000)
        store (db/mem-store)]
    (log/info "Starting HTTP listener on port: " port)
    (run-jetty (build-routes store) {:port port :join? false})))
