(ns backend.api.v1.patients
  (:require [backend.db.core :as db]
            [cheshire.core :as json]
            [clojure.spec.alpha :as s]
            [taoensso.timbre :as log])
  (:import [java.util UUID]))

;; Specs
(s/def ::name string?)
(s/def ::email (s/and string? #(re-matches #".+@.+\..+" %)))
(s/def ::phone string?)
(s/def ::date-of-birth inst?)

(s/def ::patient-create
  (s/keys :req-un [::name ::email ::phone ::date-of-birth]))

(s/def ::patient-update
  (s/keys :opt-un [::name ::email ::phone ::date-of-birth]))

(defn- validate-request [spec data]
  (when-not (s/valid? spec data)
    (throw (ex-info "Invalid request data"
                    {:type :validation-error
                     :errors (s/explain-data spec data)}))))

(defn list-patients [request]
  (try
    (let [patients (db/list-patients)]
      {:status 200
       :body {:patients patients}})
    (catch Exception e
      (log/error "Error listing patients:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn create-patient [request]
  (try
    (let [patient-data (:body request)]
      (validate-request ::patient-create patient-data)
      (let [patient-id (db/create-patient! patient-data)]
        {:status 201
         :body {:patient-id (str patient-id)
                :message "Patient created successfully"}}))
    (catch clojure.lang.ExceptionInfo e
      (if (= (:type (ex-data e)) :validation-error)
        {:status 400
         :body {:error "Validation failed"
                :details (:errors (ex-data e))}}
        (do
          (log/error "Error creating patient:" (.getMessage e))
          {:status 500
           :body {:error "Internal server error"}})))
    (catch Exception e
      (log/error "Error creating patient:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn get-patient [id request]
  (try
    (let [patient-id (UUID/fromString id)
          patient (db/get-patient patient-id)]
      (if patient
        {:status 200
         :body {:patient patient}}
        {:status 404
         :body {:error "Patient not found"}}))
    (catch IllegalArgumentException e
      {:status 400
       :body {:error "Invalid patient ID format"}})
    (catch Exception e
      (log/error "Error getting patient:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn update-patient [id request]
  (try
    (let [patient-id (UUID/fromString id)
          updates (:body request)]
      (validate-request ::patient-update updates)
      (when (db/get-patient patient-id)
        (db/update-patient! patient-id updates)
        {:status 200
         :body {:message "Patient updated successfully"}}))
    (catch IllegalArgumentException e
      {:status 400
       :body {:error "Invalid patient ID format"}})
    (catch clojure.lang.ExceptionInfo e
      (if (= (:type (ex-data e)) :validation-error)
        {:status 400
         :body {:error "Validation failed"
                :details (:errors (ex-data e))}}
        (do
          (log/error "Error updating patient:" (.getMessage e))
          {:status 500
           :body {:error "Internal server error"}})))
    (catch Exception e
      (log/error "Error updating patient:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn delete-patient [id request]
  (try
    (let [patient-id (UUID/fromString id)]
      (when (db/get-patient patient-id)
        (db/delete-patient! patient-id)
        {:status 200
         :body {:message "Patient deleted successfully"}}))
    (catch IllegalArgumentException e
      {:status 400
       :body {:error "Invalid patient ID format"}})
    (catch Exception e
      (log/error "Error deleting patient:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn list-recent-patients [request]
  "Get recently added or updated patients for dashboard"
  (try
    (let [patients (db/list-patients)
          recent (take 5 patients)]
      {:status 200
       :body {:patients recent}})
    (catch Exception e
      (log/error "Error listing recent patients:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))
