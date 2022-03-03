(ns signably.auth
  "OAuth2 login setup and middleware"
  (:require [ring.middleware.oauth2 :refer [wrap-oauth2]]
            [config.core :refer [env]]))

(defn wrap-google-auth
  "Decorate given handler to require oauth2 Google login"
  [handler]
  (-> handler
      (wrap-oauth2
       {:google
        {:authorize-uri    "https://accounts.google.com/o/oauth2/v2/auth"
         :access-token-uri "https://www.googleapis.com/oauth2/v4/token"
         :client-id        (env :google-auth-client-id)
         :client-secret    (env :google-auth-client-secret)
         :scopes           ["https://www.googleapis.com/auth/userinfo.email"
                            "https://www.googleapis.com/auth/userinfo.profile"]
         :launch-uri       "/oauth2/google"
         :redirect-uri     "/oauth2/google/callback"
         :landing-uri      "/"}})))
