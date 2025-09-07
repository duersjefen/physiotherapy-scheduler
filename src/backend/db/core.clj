(ns backend.db.core
  (:require [backend.config :as config]
            [taoensso.timbre :as log]
            [clojure.string :as str])
  (:import [java.sql DriverManager Connection PreparedStatement ResultSet]
           [java.util Date UUID]))

(def ^:private connection (atom nil))

(defn get-connection []
  (if-let [conn @connection]
    conn
        (let [db-uri (config/database-uri)
          conn (DriverManager/getConnection db-uri)]
      (reset! connection conn)
      conn)))

(defn execute-query! [sql & params]
  (let [conn (get-connection)]
    (with-open [stmt (.prepareStatement conn sql)]
      (doseq [[i param] (map-indexed vector params)]
        (.setObject stmt (inc i) param))
      (.executeUpdate stmt))))

(defn query [sql & params]
  (let [conn (get-connection)]
    (with-open [stmt (.prepareStatement conn sql)
                rs (.executeQuery
                    (do (doseq [[i param] (map-indexed vector params)]
                          (.setObject stmt (inc i) param))
                        stmt))]
      (loop [results []]
        (if (.next rs)
          (recur (conj results {:id (.getString rs "id")
                               :name (.getString rs "name")
                               :email (.getString rs "email")
                               :phone (.getString rs "phone")
                               :date_of_birth (.getString rs "date_of_birth")
                               :created_at (.getString rs "created_at")}))
          results)))))

(defn query-one [sql & params]
  (first (apply query sql params)))

(def schema-sql
  ["CREATE TABLE IF NOT EXISTS patients (
     id TEXT PRIMARY KEY,
     name TEXT NOT NULL,
     email TEXT UNIQUE NOT NULL,
     phone TEXT,
     date_of_birth TEXT,
     created_at TEXT DEFAULT CURRENT_TIMESTAMP
   );"
   
   "CREATE TABLE IF NOT EXISTS slots (
     id TEXT PRIMARY KEY,
     start_time TEXT NOT NULL,
     end_time TEXT NOT NULL,
     available INTEGER DEFAULT 1,
     created_at TEXT DEFAULT CURRENT_TIMESTAMP
   );"
   
   "CREATE TABLE IF NOT EXISTS appointments (
     id TEXT PRIMARY KEY,
     patient_id TEXT NOT NULL,
     slot_id TEXT NOT NULL,
     status TEXT DEFAULT 'scheduled',
     notes TEXT,
     created_at TEXT DEFAULT CURRENT_TIMESTAMP,
     FOREIGN KEY (patient_id) REFERENCES patients(id),
     FOREIGN KEY (slot_id) REFERENCES slots(id)
   );"
   
   "CREATE TABLE IF NOT EXISTS massage_bookings (
     id TEXT PRIMARY KEY,
     name TEXT NOT NULL,
     email TEXT NOT NULL,
     phone TEXT NOT NULL,
     service_type TEXT NOT NULL,
     booking_type TEXT NOT NULL,
     notes TEXT,
     status TEXT DEFAULT 'pending',
     created_at TEXT DEFAULT CURRENT_TIMESTAMP
   );"])

(defn init-db! []
  (try
    (log/info "Initializing SQLite database...")
    (let [conn (get-connection)]
      (doseq [sql schema-sql]
        (with-open [stmt (.createStatement conn)]
          (.execute stmt sql)))
      (log/info "Database initialized successfully"))
    (catch Exception e
      (log/error "Failed to initialize database:" (.getMessage e))
      (throw e))))

;; Patient operations
(defn create-patient! [patient-data]
  (let [patient-id (str (UUID/randomUUID))]
    (execute-query! 
     "INSERT INTO patients (id, name, email, phone, date_of_birth) VALUES (?, ?, ?, ?, ?)"
     patient-id
     (:name patient-data)
     (:email patient-data)
     (:phone patient-data)
     (str (:date-of-birth patient-data)))
    patient-id))

(defn get-patient [patient-id]
  (query-one "SELECT * FROM patients WHERE id = ?" patient-id))

(defn list-patients []
  (query "SELECT * FROM patients ORDER BY created_at DESC"))

(defn update-patient! [patient-id updates]
  (let [set-clauses (map #(str (name %) " = ?") (keys updates))
        sql (str "UPDATE patients SET " (clojure.string/join ", " set-clauses) " WHERE id = ?")]
    (apply execute-query! sql (concat (vals updates) [patient-id]))))

(defn delete-patient! [patient-id]
  (execute-query! "DELETE FROM patients WHERE id = ?" patient-id))

;; Slot operations
(defn create-slot! [slot-data]
  (let [slot-id (str (UUID/randomUUID))]
    (execute-query!
     "INSERT INTO slots (id, start_time, end_time) VALUES (?, ?, ?)"
     slot-id
     (str (:start-time slot-data))
     (str (:end-time slot-data)))
    slot-id))

(defn get-slot [slot-id]
  (query-one "SELECT * FROM slots WHERE id = ?" slot-id))

(defn list-slots []
  (query "SELECT * FROM slots ORDER BY start_time"))

(defn update-slot! [slot-id updates]
  (let [set-clauses (map #(str (name %) " = ?") (keys updates))
        sql (str "UPDATE slots SET " (clojure.string/join ", " set-clauses) " WHERE id = ?")]
    (apply execute-query! sql (concat (vals updates) [slot-id]))))

(defn delete-slot! [slot-id]
  (execute-query! "DELETE FROM slots WHERE id = ?" slot-id))

;; Appointment operations
(defn create-appointment! [appointment-data]
  (let [appointment-id (str (UUID/randomUUID))]
    (execute-query!
     "INSERT INTO appointments (id, patient_id, slot_id, notes) VALUES (?, ?, ?, ?)"
     appointment-id
     (:patient-id appointment-data)
     (:slot-id appointment-data)
     (:notes appointment-data ""))
    ;; Mark slot as unavailable
    (execute-query! "UPDATE slots SET available = 0 WHERE id = ?" (:slot-id appointment-data))
    appointment-id))

(defn get-appointment [appointment-id]
  (query-one 
   "SELECT a.*, p.name as patient_name, s.start_time, s.end_time
    FROM appointments a 
    JOIN patients p ON a.patient_id = p.id 
    JOIN slots s ON a.slot_id = s.id 
    WHERE a.id = ?" 
   appointment-id))

(defn list-appointments []
  (query 
   "SELECT a.*, p.name as patient_name, s.start_time, s.end_time
    FROM appointments a 
    JOIN patients p ON a.patient_id = p.id 
    JOIN slots s ON a.slot_id = s.id 
    ORDER BY s.start_time"))

;; Massage booking operations
(defn create-massage-booking! [booking-data]
  (execute-query! 
   "INSERT INTO massage_bookings (id, name, email, phone, service_type, booking_type, notes, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
   (:id booking-data)
   (:name booking-data)
   (:email booking-data)
   (:phone booking-data)
   (name (:service-type booking-data))
   (name (:booking-type booking-data))
   (:notes booking-data)
   (name (:status booking-data))
   (str (:created-at booking-data))))

(defn list-massage-bookings []
  (query "SELECT * FROM massage_bookings ORDER BY created_at DESC"))

(defn get-massage-booking [booking-id]
  (query-one "SELECT * FROM massage_bookings WHERE id = ?" booking-id))

(defn update-appointment! [appointment-id updates]
  (let [set-clauses (map #(str (name %) " = ?") (keys updates))
        sql (str "UPDATE appointments SET " (clojure.string/join ", " set-clauses) " WHERE id = ?")]
    (apply execute-query! sql (concat (vals updates) [appointment-id]))))

(defn delete-appointment! [appointment-id]
  (let [appointment (get-appointment appointment-id)]
    (execute-query! "DELETE FROM appointments WHERE id = ?" appointment-id)
    ;; Mark slot as available again
    (when-let [slot-id (:slot_id appointment)]
      (execute-query! "UPDATE slots SET available = 1 WHERE id = ?" slot-id))))
