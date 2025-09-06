(ns backend.middleware.auth
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]))

(defn unauthorized-handler [request metadata]
  {:status 401
   :headers {"Content-Type" "application/json"}
   :body {:error "Unauthorized"}})

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))

(defn wrap-auth [handler]
  (-> handler
      (wrap-authentication auth-backend)
      (wrap-authorization auth-backend)))

(defn require-auth [handler]
  (fn [request]
    (if (authenticated? request)
      (handler request)
      {:status 401
       :body {:error "Authentication required"}})))
