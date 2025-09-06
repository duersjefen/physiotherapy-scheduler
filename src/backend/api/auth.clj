(ns backend.api.auth
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.sign.jwt :as jwt]
            [backend.config :as config]
            [cheshire.core :as json]
            [taoensso.timbre :as log]))

;; Simple authentication for demo - in production use proper user management
(def demo-users
  {"admin" {:password "admin123" :role :admin}
   "therapist" {:password "therapist123" :role :therapist}})

(defn authenticate-user [username password]
  (when-let [user (get demo-users username)]
    (when (= (:password user) password)
      (dissoc user :password))))

(defn create-token [user]
  (jwt/sign {:user user
             :exp (+ (System/currentTimeMillis) 
                     (* 1000 60 (config/session-timeout)))}
            (config/auth-secret)))

(defn login [request]
  (try
    (let [{:keys [username password]} (:body request)]
      (if-let [user (authenticate-user username password)]
        (let [token (create-token user)]
          {:status 200
           :session {:user user}
           :body {:token token
                  :user user
                  :message "Login successful"}})
        {:status 401
         :body {:error "Invalid credentials"}}))
    (catch Exception e
      (log/error "Error during login:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn logout [request]
  {:status 200
   :session nil
   :body {:message "Logout successful"}})

(defn current-user [request]
  (if-let [user (get-in request [:session :user])]
    {:status 200
     :body {:user user}}
    {:status 401
     :body {:error "Not authenticated"}}))
