(ns app.db.slots
  "Database operations for appointment slots"
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [app.db.core :as db]
            [clj-time.core :as time]
            [clj-time.format :as fmt]
            [clj-time.coerce :as coerce]
            [taoensso.timbre :as log])
  (:import [java.util UUID]))

;; Date/time utilities
(def iso8601-formatter (fmt/formatters :date-time))

(defn now-iso8601
  "Get current timestamp in ISO8601 format"
  []
  (fmt/unparse iso8601-formatter (time/now)))

(defn parse-iso8601
  "Parse ISO8601 timestamp"
  [timestamp]
  (fmt/parse iso8601-formatter timestamp))

;; Database operations
(defn create-slot!
  "Create a new appointment slot"
  [start-time duration-minutes]
  (let [now (now-iso8601)
        slot-data {:start_time start-time
                   :duration_minutes duration-minutes
                   :status "available"
                   :created_at now
                   :updated_at now}]
    (try
      (with-open [conn (db/get-connection)]
        (let [result (sql/insert! conn :appointments slot-data {:return-keys true})]
          (log/info "Created slot:" slot-data)
          (first result)))
      (catch Exception e
        (log/error e "Failed to create slot:" slot-data)
        (throw e)))))

(defn get-slot-by-id
  "Get appointment slot by ID"
  [id]
  (try
    (with-open [conn (db/get-connection)]
      (sql/get-by-id conn :appointments id))
    (catch Exception e
      (log/error e "Failed to get slot by ID:" id)
      nil)))

(defn get-available-slots
  "Get available slots within date range with pagination"
  [start-date end-date & {:keys [limit offset] :or {limit 50 offset 0}}]
  (try
    (with-open [conn (db/get-connection)]
      (jdbc/execute! conn
        ["SELECT id, start_time, duration_minutes
          FROM appointments 
          WHERE status = 'available' 
          AND start_time >= ? 
          AND start_time <= ?
          AND start_time > ?
          ORDER BY start_time ASC
          LIMIT ? OFFSET ?"
         start-date
         end-date
         (now-iso8601)  ; Only future slots
         limit
         offset]))
    (catch Exception e
      (log/error e "Failed to get available slots")
      [])))

(defn book-slot!
  "Book an appointment slot with transaction safety"
  [slot-id client-name client-email]
  (try
    (with-open [conn (db/get-connection)]
      (jdbc/with-transaction [tx conn]
        ;; Check if slot is still available
        (let [slot (sql/get-by-id tx :appointments slot-id)]
          (cond
            (nil? slot)
            {:success false :error "Slot not found" :status 404}
            
            (not= (:status slot) "available")
            {:success false :error "Slot already booked" :status 400}
            
            ;; Check if slot is in the past
            (time/before? (parse-iso8601 (:start_time slot)) (time/now))
            {:success false :error "Cannot book past slots" :status 400}
            
            :else
            ;; Book the slot
            (let [now (now-iso8601)
                  updated-slot (sql/update! tx :appointments
                                {:client_name client-name
                                 :client_email client-email
                                 :status "booked"
                                 :updated_at now}
                                {:id slot-id})]
              (log/info "Booked slot:" slot-id "for client:" client-name)
              {:success true :slot (merge slot {:client_name client-name
                                               :client_email client-email
                                               :status "booked"
                                               :updated_at now})})))))
    (catch Exception e
      (log/error e "Failed to book slot:" slot-id)
      {:success false :error "Internal server error" :status 500})))

(defn get-all-slots
  "Get all slots with optional status filter"
  [& {:keys [status limit offset] :or {limit 100 offset 0}}]
  (try
    (with-open [conn (db/get-connection)]
      (if status
        (jdbc/execute! conn
          ["SELECT * FROM appointments WHERE status = ? ORDER BY start_time DESC LIMIT ? OFFSET ?"
           status limit offset])
        (jdbc/execute! conn
          ["SELECT * FROM appointments ORDER BY start_time DESC LIMIT ? OFFSET ?"
           limit offset])))
    (catch Exception e
      (log/error e "Failed to get all slots")
      [])))

(defn cancel-slot!
  "Cancel a booked appointment slot"
  [slot-id]
  (try
    (with-open [conn (db/get-connection)]
      (let [updated (sql/update! conn :appointments
                      {:status "cancelled"
                       :updated_at (now-iso8601)}
                      {:id slot-id})]
        (if (pos? (first updated))
          (do
            (log/info "Cancelled slot:" slot-id)
            {:success true})
          {:success false :error "Slot not found" :status 404})))
    (catch Exception e
      (log/error e "Failed to cancel slot:" slot-id)
      {:success false :error "Internal server error" :status 500})))
