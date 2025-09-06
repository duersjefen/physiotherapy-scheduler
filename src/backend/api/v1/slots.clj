(ns backend.api.v1.slots
  (:require [backend.db.core :as db]
            [cheshire.core :as json]
            [clojure.spec.alpha :as s]
            [taoensso.timbre :as log]))

;; Specs
(s/def ::start-time inst?)
(s/def ::end-time inst?)
(s/def ::available boolean?)

(s/def ::slot-create
  (s/keys :req-un [::start-time ::end-time]))

(s/def ::slot-update
  (s/keys :opt-un [::start-time ::end-time ::available]))

(defn- validate-request [spec data]
  (when-not (s/valid? spec data)
    (throw (ex-info "Invalid request data" 
                    {:type :validation-error
                     :errors (s/explain-data spec data)}))))

(defn list-slots [request]
  (try
    (let [slots (db/list-slots)]
      {:status 200
       :body {:slots slots}})
    (catch Exception e
      (log/error "Error listing slots:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn create-slot [request]
  (try
    (let [slot-data (:body request)]
      (validate-request ::slot-create slot-data)
      (let [slot-id (db/create-slot! slot-data)]
        {:status 201
         :body {:slot-id (str slot-id)
                :message "Slot created successfully"}}))
    (catch clojure.lang.ExceptionInfo e
      (if (= (:type (ex-data e)) :validation-error)
        {:status 400
         :body {:error "Validation failed"
                :details (:errors (ex-data e))}}
        (do
          (log/error "Error creating slot:" (.getMessage e))
          {:status 500
           :body {:error "Internal server error"}})))
    (catch Exception e
      (log/error "Error creating slot:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn get-slot [id request]
  (try
    (let [slot-id (java.util.UUID/fromString id)
          slot (db/get-slot slot-id)]
      (if slot
        {:status 200
         :body {:slot slot}}
        {:status 404
         :body {:error "Slot not found"}}))
    (catch IllegalArgumentException e
      {:status 400
       :body {:error "Invalid slot ID format"}})
    (catch Exception e
      (log/error "Error getting slot:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn update-slot [id request]
  (try
    (let [slot-id (java.util.UUID/fromString id)
          updates (:body request)]
      (validate-request ::slot-update updates)
      (when (db/get-slot slot-id)
        (db/update-slot! slot-id updates)
        {:status 200
         :body {:message "Slot updated successfully"}}))
    (catch IllegalArgumentException e
      {:status 400
       :body {:error "Invalid slot ID format"}})
    (catch clojure.lang.ExceptionInfo e
      (if (= (:type (ex-data e)) :validation-error)
        {:status 400
         :body {:error "Validation failed"
                :details (:errors (ex-data e))}}
        (do
          (log/error "Error updating slot:" (.getMessage e))
          {:status 500
           :body {:error "Internal server error"}})))
    (catch Exception e
      (log/error "Error updating slot:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))

(defn delete-slot [id request]
  (try
    (let [slot-id (java.util.UUID/fromString id)]
      (when (db/get-slot slot-id)
        (db/delete-slot! slot-id)
        {:status 200
         :body {:message "Slot deleted successfully"}}))
    (catch IllegalArgumentException e
      {:status 400
       :body {:error "Invalid slot ID format"}})
    (catch Exception e
      (log/error "Error deleting slot:" (.getMessage e))
      {:status 500
       :body {:error "Internal server error"}})))
