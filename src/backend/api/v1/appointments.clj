(ns backend.api.v1.appointments
  (:require [backend.db.core :as db]
            [cheshire.core :as json]
            [clojure.spec.alpha :as s]
            [taoensso.timbre :as log]))

;; Specs
(s/def ::patient-id uuid?)
(s/def ::slot-id uuid?)
(s/def ::status #{:scheduled :completed :cancelled :no-show})
(s/def ::notes string?)

(s/def ::appointment-create
  (s/keys :req-un [::patient-id ::slot-id]
          :opt-un [::notes]))

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

(defn get-appointment [id request]
  (try
    (let [appointment-id (java.util.UUID/fromString id)
          appointment (db/get-appointment appointment-id)]
      (if appointment
        {:status 200
         :body {:appointment appointment}}
        {:status 404
         :body {:error "Appointment not found"}}))
    (catch IllegalArgumentException e
      {:status 400
       :body {:error "Invalid appointment ID format"}})
    (catch Exception e
      (log/error "Error getting appointment:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn update-appointment [id request]
  (try
    (let [appointment-id (java.util.UUID/fromString id)
          updates (:body request)]
      (validate-request ::appointment-update updates)
      (when (db/get-appointment appointment-id)
        (db/update-appointment! appointment-id updates)
        {:status 200
         :body {:message "Appointment updated successfully"}}))
    (catch IllegalArgumentException e
      {:status 400
       :body {:error "Invalid appointment ID format"}})
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

(defn delete-appointment [id request]
  (try
    (let [appointment-id (java.util.UUID/fromString id)]
      (when (db/get-appointment appointment-id)
        (db/delete-appointment! appointment-id)
        {:status 200
         :body {:message "Appointment deleted successfully"}}))
    (catch IllegalArgumentException e
      {:status 400
       :body {:error "Invalid appointment ID format"}})
    (catch Exception e
      (log/error "Error deleting appointment:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))
