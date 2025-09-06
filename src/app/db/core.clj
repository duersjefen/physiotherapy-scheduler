(ns app.db.core
  "Database connection and migration handling"
  (:require [next.jdbc :as jdbc]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]
            [environ.core :refer [env]]))

(def ^:private db-spec
  "Database specification for SQLite"
  {:dbtype "sqlite"
   :dbname (or (env :database-path) "scheduler.db")})

(defn get-connection
  "Get a database connection"
  []
  (jdbc/get-connection db-spec))

(defn execute!
  "Execute a SQL statement"
  [sql]
  (with-open [conn (get-connection)]
    (jdbc/execute! conn sql)))

(defn execute-one!
  "Execute a SQL statement and return one result"
  [sql]
  (with-open [conn (get-connection)]
    (jdbc/execute-one! conn sql)))

(defn run-migrations!
  "Run database migrations from resources/migrations directory"
  []
  (log/info "Running database migrations...")
  (try
    ;; For simplicity, just load the one migration file we have
    (when-let [migration-resource (io/resource "migrations/001_create_appointments.sql")]
      (log/info "Running migration: 001_create_appointments.sql")
      (let [migration-sql (slurp migration-resource)]
        (execute! [migration-sql])))
    (log/info "Database migrations completed successfully")
    (catch Exception e
      (log/error e "Failed to run database migrations")
      (throw e))))

(defn init-db!
  "Initialize database and run migrations"
  []
  (log/info "Initializing database...")
  (run-migrations!)
  (log/info "Database initialization complete"))

(defn health-check
  "Check database health"
  []
  (try
    (execute-one! ["SELECT 1 as healthy"])
    {:status :healthy}
    (catch Exception e
      (log/error e "Database health check failed")
      {:status :unhealthy :error (.getMessage e)})))
