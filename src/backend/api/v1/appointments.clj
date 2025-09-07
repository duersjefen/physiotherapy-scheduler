(ns backend.api.v1.appointments
  (:require [backend.db.core :as db]
            [cheshire.core :as json]
            [clojure.spec.alpha :as s]
            [taoensso.timbre :as log])
  (:import [java.util UUID Date]))

;; Specs
(s/def ::patient-id string?)
(s/def ::slot-id string?)
(s/def ::status #{:scheduled :completed :cancelled :no-show})
(s/def ::notes string?)
(s/def ::name string?)
(s/def ::email string?)
(s/def ::phone string?)
(s/def ::service string?)
(s/def ::description string?)

(s/def ::appointment-create
  (s/keys :req-un [::patient-id ::slot-id]
          :opt-un [::notes]))

(s/def ::appointment-request
  (s/keys :req-un [::name ::email ::phone]
          :opt-un [::service ::description]))

(s/def ::appointment-update
  (s/keys :opt-un [::status ::notes]))

(defn- validate-request [spec data]
  (when-not (s/valid? spec data)
    (throw (ex-info "Invalid request data" 
                    {:type :validation-error
                     :errors (s/explain-data spec data)}))))

(defn list-appointments [request]
  (try
    (let [appointments (db/list-appointments)]
      {:status 200
       :body {:appointments appointments}})
    (catch Exception e
      (log/error "Error listing appointments:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn list-upcoming-appointments [request]
  (try
    (let [appointments (db/list-appointments)
          ;; Filter upcoming appointments (simplified for now)
          upcoming (take 10 appointments)]
      {:status 200
       :body {:appointments upcoming}})
    (catch Exception e
      (log/error "Error listing upcoming appointments:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn create-appointment-request [request]
  "Handle public appointment requests from the website"
  (try
    (let [request-data (:body request)]
      (validate-request ::appointment-request request-data)
      ;; For now, just log the request - in a real app, this would:
      ;; 1. Create a pending appointment request
      ;; 2. Send email notification to clinic
      ;; 3. Create patient record if new
      (log/info "New appointment request received:" request-data)
      {:status 200
       :body {:message "Appointment request received! We'll contact you within 24 hours to confirm your appointment."
              :request-id (str (UUID/randomUUID))}})
    (catch clojure.lang.ExceptionInfo e
      (if (= (:type (ex-data e)) :validation-error)
        {:status 400
         :body {:error "Please fill in all required fields"
                :details (:errors (ex-data e))}}
        (do
          (log/error "Error processing appointment request:" (.getMessage e))
          {:status 500
           :body {:error "Failed to submit appointment request. Please try again."}})))
    (catch Exception e
      (log/error "Error processing appointment request:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn create-appointment [request]
  (try
    (let [appointment-data (:body request)]
      (validate-request ::appointment-create appointment-data)
      (let [appointment-id (db/create-appointment! appointment-data)]
        {:status 201
         :body {:appointment-id (str appointment-id)
                :message "Appointment created successfully"}}))
    (catch clojure.lang.ExceptionInfo e
      (if (= (:type (ex-data e)) :validation-error)
        {:status 400
         :body {:error "Validation failed"
                :details (:errors (ex-data e))}}
        (do
          (log/error "Error creating appointment:" (.getMessage e))
          {:status 500
           :body {:error "Internal server error"}})))
    (catch Exception e
      (log/error "Error creating appointment:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn get-appointment [appointment-id request]
  (try
    (let [appointment (db/get-appointment appointment-id)]
      (if appointment
        {:status 200
         :body {:appointment appointment}}
        {:status 404
         :body {:error "Appointment not found"}}))
    (catch Exception e
      (log/error "Error getting appointment:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn update-appointment [appointment-id request]
  (try
    (let [update-data (:body request)]
      (validate-request ::appointment-update update-data)
      (db/update-appointment! appointment-id update-data)
      {:status 200
       :body {:message "Appointment updated successfully"}})
    (catch clojure.lang.ExceptionInfo e
      (if (= (:type (ex-data e)) :validation-error)
        {:status 400
         :body {:error "Validation failed"
                :details (:errors (ex-data e))}}
        (do
          (log/error "Error updating appointment:" (.getMessage e))
          {:status 500
           :body {:error "Internal server error"}})))
    (catch Exception e
      (log/error "Error updating appointment:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn delete-appointment [appointment-id request]
  (try
    (db/delete-appointment! appointment-id)
    {:status 200
     :body {:message "Appointment deleted successfully"}}
    (catch Exception e
      (log/error "Error deleting appointment:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

;; Massage booking specs
(s/def ::service-type #{:swedish :deep-tissue :sports :hot-stone :prenatal :aromatherapy})
(s/def ::booking-type #{:massage})

(s/def ::massage-booking
  (s/keys :req-un [::name ::email ::phone ::service-type ::booking-type]
          :opt-un [::notes]))

(defn create-massage-booking [request]
  (try
    (let [booking-data (:body request)]
      (validate-request ::massage-booking booking-data)
      (let [booking-id (str (UUID/randomUUID))
            booking-record (merge booking-data
                                  {:id booking-id
                                   :created-at (Date.)
                                   :status :pending})]
        (db/create-massage-booking! booking-record)
        (log/info "Massage booking created successfully:" booking-id)
        {:status 201
         :body {:message "Massage booking created successfully"
                :booking-id booking-id}}))
    (catch clojure.lang.ExceptionInfo e
      (if (= (:type (ex-data e)) :validation-error)
        {:status 400
         :body {:error "Validation failed"
                :details (:errors (ex-data e))}}
        (do
          (log/error "Error creating massage booking:" (.getMessage e))
          {:status 500
           :body {:error "Internal server error"}})))
    (catch Exception e
      (log/error "Error creating massage booking:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))
