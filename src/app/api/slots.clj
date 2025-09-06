(ns app.api.slots
  "API endpoints for appointment slots"
  (:require [compojure.core :refer [GET POST PUT DELETE]]
            [ring.util.response :as response]
            [cheshire.core :as json]
            [app.db.slots :as slots]
            [clj-time.format :as fmt]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

;; Validation utilities
(defn valid-iso8601?
  "Check if string is valid ISO8601 timestamp"
  [timestamp]
  (try
    (fmt/parse (fmt/formatters :date-time) timestamp)
    true
    (catch Exception _ false)))

(defn valid-email?
  "Basic email validation"
  [email]
  (and email
       (string? email)
       (re-matches #"[^@]+@[^@]+\.[^@]+" email)))

(defn validate-create-slot
  "Validate slot creation request"
  [body]
  (let [{:keys [start-time duration]} body]
    (cond
      (not start-time)
      {:valid false :error "start-time is required"}
      
      (not (valid-iso8601? start-time))
      {:valid false :error "start-time must be valid ISO8601 format"}
      
      (not duration)
      {:valid false :error "duration is required"}
      
      (not (and (number? duration) (pos? duration)))
      {:valid false :error "duration must be a positive number"}
      
      :else
      {:valid true})))

(defn validate-book-slot
  "Validate slot booking request"
  [body]
  (let [{:keys [client-name client-email]} body]
    (cond
      (str/blank? client-name)
      {:valid false :error "client-name is required"}
      
      (str/blank? client-email)
      {:valid false :error "client-email is required"}
      
      (not (valid-email? client-email))
      {:valid false :error "client-email must be valid email address"}
      
      :else
      {:valid true})))

;; Response utilities
(defn json-response
  "Create JSON response"
  ([data] (json-response data 200))
  ([data status]
   (-> (response/response (json/generate-string data))
       (response/content-type "application/json")
       (response/status status))))

(defn error-response
  "Create error response"
  [error status]
  (json-response {:error error} status))

;; API routes
(defn get-available-slots
  "GET /api/slots/available - Get available appointment slots"
  [request]
  (let [params (:query-params request)
        start-date (get params "start-date")
        end-date (get params "end-date")
        limit (when-let [l (get params "limit")] (Integer/parseInt l))
        offset (when-let [o (get params "offset")] (Integer/parseInt o))]
    
    (cond
      (not start-date)
      (error-response "start-date parameter is required" 400)
      
      (not end-date)
      (error-response "end-date parameter is required" 400)
      
      (not (valid-iso8601? start-date))
      (error-response "start-date must be valid ISO8601 format" 400)
      
      (not (valid-iso8601? end-date))
      (error-response "end-date must be valid ISO8601 format" 400)
      
      :else
      (try
        (let [slots (slots/get-available-slots start-date end-date
                                              :limit (or limit 50)
                                              :offset (or offset 0))]
          (json-response {:slots slots
                         :count (count slots)}))
        (catch Exception e
          (log/error e "Failed to get available slots")
          (error-response "Internal server error" 500))))))

(defn create-slot
  "POST /api/slots - Create new appointment slot (Admin only)"
  [request]
  (try
    (let [body (json/parse-string (slurp (:body request)) true)
          validation (validate-create-slot body)]
      
      (if (:valid validation)
        (let [slot (slots/create-slot! (:start-time body) (:duration body))]
          (json-response {:slot slot} 201))
        (error-response (:error validation) 400)))
    
    (catch Exception e
      (log/error e "Failed to create slot")
      (error-response "Internal server error" 500))))

(defn book-slot
  "POST /api/slots/:id/book - Book an appointment slot"
  [request]
  (try
    (let [slot-id (Integer/parseInt (get-in request [:route-params :id]))
          body (json/parse-string (slurp (:body request)) true)
          validation (validate-book-slot body)]
      
      (if (:valid validation)
        (let [result (slots/book-slot! slot-id (:client-name body) (:client-email body))]
          (if (:success result)
            (json-response {:slot (:slot result)} 200)
            (error-response (:error result) (:status result))))
        (error-response (:error validation) 400)))
    
    (catch NumberFormatException _
      (error-response "Invalid slot ID" 400))
    (catch Exception e
      (log/error e "Failed to book slot")
      (error-response "Internal server error" 500))))

(defn get-slot-details
  "GET /api/slots/:id - Get slot details by ID"
  [request]
  (try
    (let [slot-id (Integer/parseInt (get-in request [:route-params :id]))
          slot (slots/get-slot-by-id slot-id)]
      
      (if slot
        (json-response {:slot slot})
        (error-response "Slot not found" 404)))
    
    (catch NumberFormatException _
      (error-response "Invalid slot ID" 400))
    (catch Exception e
      (log/error e "Failed to get slot details")
      (error-response "Internal server error" 500))))

(defn get-all-slots-admin
  "GET /api/admin/slots - Get all slots for admin view"
  [request]
  (try
    (let [params (:query-params request)
          status (get params "status")
          limit (when-let [l (get params "limit")] (Integer/parseInt l))
          offset (when-let [o (get params "offset")] (Integer/parseInt o))
          slots (slots/get-all-slots :status status
                                    :limit (or limit 100)
                                    :offset (or offset 0))]
      (json-response {:slots slots
                     :count (count slots)}))
    (catch Exception e
      (log/error e "Failed to get admin slots")
      (error-response "Internal server error" 500))))

;; Route definitions
(def routes
  "API routes for slots"
  [(GET "/api/slots/available" request (get-available-slots request))
   (POST "/api/slots" request (create-slot request))
   (POST "/api/slots/:id/book" request (book-slot request))
   (GET "/api/slots/:id" request (get-slot-details request))
   (GET "/api/admin/slots" request (get-all-slots-admin request))])
