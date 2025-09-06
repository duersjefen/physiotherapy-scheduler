(ns backend.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [compojure.core :refer [defroutes GET POST PUT DELETE]]
            [compojure.route :as route]
            [taoensso.timbre :as log]
            [backend.config :as config]
            [backend.db.core :as db]
            [backend.api.v1.patients :as patients]
            [backend.api.v1.appointments :as appointments]
            [backend.api.v1.slots :as slots]
            [backend.api.auth :as auth]
            [backend.middleware.auth :as auth-middleware]
            [backend.middleware.error-handling :as error-handling]))

(defroutes app-routes
  ;; Health check
  (GET "/health" [] {:status 200 :body {:status "ok"}})
  
  ;; Auth endpoints
  (POST "/api/auth/login" request (auth/login request))
  (POST "/api/auth/logout" request (auth/logout request))
  
  ;; V1 API endpoints (protected)
  (GET "/api/v1/patients" request (patients/list-patients request))
  (POST "/api/v1/patients" request (patients/create-patient request))
  (GET "/api/v1/patients/:id" [id :as request] (patients/get-patient id request))
  (PUT "/api/v1/patients/:id" [id :as request] (patients/update-patient id request))
  (DELETE "/api/v1/patients/:id" [id :as request] (patients/delete-patient id request))
  
  (GET "/api/v1/appointments" request (appointments/list-appointments request))
  (POST "/api/v1/appointments" request (appointments/create-appointment request))
  (GET "/api/v1/appointments/:id" [id :as request] (appointments/get-appointment id request))
  (PUT "/api/v1/appointments/:id" [id :as request] (appointments/update-appointment id request))
  (DELETE "/api/v1/appointments/:id" [id :as request] (appointments/delete-appointment id request))
  
  (GET "/api/v1/slots" request (slots/list-slots request))
  (POST "/api/v1/slots" request (slots/create-slot request))
  (GET "/api/v1/slots/:id" [id :as request] (slots/get-slot id request))
  (PUT "/api/v1/slots/:id" [id :as request] (slots/update-slot id request))
  (DELETE "/api/v1/slots/:id" [id :as request] (slots/delete-slot id request))
  
  ;; Serve static files
  (route/resources "/")
  (route/not-found {:status 404 :body {:error "Not found"}}))

(def app
  (-> app-routes
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :post :put :delete]
                 :access-control-allow-headers ["Content-Type" "Authorization"])
      (auth-middleware/wrap-auth)
      (wrap-json-response)
      (wrap-json-body {:keywords? true})
      (error-handling/wrap-error-handling)
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))

(defn start-server []
  (let [port (get-in (config/load-config) [:server :port])]
    (log/info "Starting server on port" port)
    (db/init-db!)
    (jetty/run-jetty app {:port port :join? false})))

(defn -main [& args]
  (start-server))
