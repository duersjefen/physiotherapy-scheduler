(ns app.core
  "Main application entry point"
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.util.response :as response]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [app.api.slots :as slots-api]
            [app.db.core :as db]
            [taoensso.timbre :as log]
            [environ.core :refer [env]])
  (:gen-class))

;; Health check endpoint
(defn health-check
  "Health check endpoint"
  [_]
  (let [db-health (db/health-check)]
    (if (= (:status db-health) :healthy)
      (response/response {:status "healthy" :database "connected"})
      (-> (response/response {:status "unhealthy" :database (:error db-health)})
          (response/status 503)))))

;; Main routes
(defroutes app-routes
  ;; Health check
  (GET "/health" request (health-check request))
  
  ;; API routes
  (GET "/api/slots/available" request (slots-api/get-available-slots request))
  (POST "/api/slots" request (slots-api/create-slot request))
  (POST "/api/slots/:id/book" request (slots-api/book-slot request))
  (GET "/api/slots/:id" request (slots-api/get-slot-details request))
  (GET "/api/admin/slots" request (slots-api/get-all-slots-admin request))
  
  ;; Serve index.html for all other routes (SPA)
  (GET "/*" []
    (-> (response/resource-response "public/index.html")
        (response/content-type "text/html")))
  
  ;; 404 for API routes
  (route/not-found {:status 404 :body {:error "Not found"}}))

;; Middleware stack
(def app
  "Application with middleware"
  (-> app-routes
      (wrap-json-body {:keywords? true})
      wrap-json-response
      (wrap-resource "public")
      wrap-content-type
      wrap-not-modified
      (wrap-defaults (-> site-defaults
                        (assoc-in [:security :anti-forgery] false)
                        (assoc-in [:static :resources] false)))))

;; Server management
(defonce server (atom nil))

(defn start-server!
  "Start the web server"
  [& {:keys [port] :or {port 3000}}]
  (let [port (Integer/parseInt (str (or (env :port) port)))]
    (log/info "Starting server on port" port)
    (reset! server
      (jetty/run-jetty app
        {:port port
         :join? false
         :send-server-version? false}))
    (log/info "Server started successfully on port" port)))

(defn stop-server!
  "Stop the web server"
  []
  (when @server
    (log/info "Stopping server...")
    (.stop @server)
    (reset! server nil)
    (log/info "Server stopped")))

(defn restart-server!
  "Restart the web server"
  []
  (stop-server!)
  (start-server!))

;; Application lifecycle
(defn init!
  "Initialize the application"
  []
  (log/info "Initializing Physiotherapy Scheduler...")
  (try
    (db/init-db!)
    (log/info "Application initialized successfully")
    (catch Exception e
      (log/error e "Failed to initialize application")
      (System/exit 1))))

(defn shutdown!
  "Shutdown the application gracefully"
  []
  (log/info "Shutting down application...")
  (stop-server!)
  (log/info "Application shutdown complete"))

;; Main entry point
(defn -main
  "Main entry point"
  [& args]
  (let [port (or (env :port) 3000)]
    (init!)
    
    ;; Add shutdown hook
    (.addShutdownHook (Runtime/getRuntime)
      (Thread. shutdown!))
    
    ;; Start server
    (start-server! :port port)
    
    (log/info "=== Physiotherapy Scheduler Started ===")
    (log/info "Server running on port:" port)
    (log/info "Health check: http://localhost:" port "/health")))
