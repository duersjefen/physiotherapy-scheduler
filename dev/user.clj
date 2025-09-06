(ns user
  "Development namespace for REPL workflow"
  (:require [app.core :as core]
            [app.db.core :as db]
            [app.db.slots :as slots]
            [clojure.tools.namespace.repl :refer [refresh]]
            [taoensso.timbre :as log]))

(log/info "Loading development environment...")

;; Development helpers
(defn start
  "Start the development server"
  []
  (core/init!)
  (core/start-server! :port 3000)
  (log/info "Development server started on http://localhost:3000"))

(defn stop
  "Stop the development server"
  []
  (core/stop-server!))

(defn restart
  "Restart the development server"
  []
  (stop)
  (refresh)
  (start))

(defn reset-db
  "Reset database (for development)"
  []
  (db/init-db!)
  (log/info "Database reset complete"))

(defn create-sample-slots
  "Create sample appointment slots for testing"
  []
  (let [base-time "2024-12-01T09:00:00Z"
        slots-data [
          {:start-time "2024-12-01T09:00:00Z" :duration 60}
          {:start-time "2024-12-01T10:00:00Z" :duration 60}
          {:start-time "2024-12-01T11:00:00Z" :duration 60}
          {:start-time "2024-12-01T14:00:00Z" :duration 60}
          {:start-time "2024-12-01T15:00:00Z" :duration 60}
          {:start-time "2024-12-02T09:00:00Z" :duration 60}
          {:start-time "2024-12-02T10:00:00Z" :duration 60}
          {:start-time "2024-12-02T11:00:00Z" :duration 60}]]
    (doseq [slot-data slots-data]
      (slots/create-slot! (:start-time slot-data) (:duration slot-data)))
    (log/info "Created" (count slots-data) "sample slots")))

(defn dev-setup
  "Setup development environment"
  []
  (reset-db)
  (create-sample-slots)
  (start)
  (log/info "Development environment ready!"))

;; Print helpful commands
(log/info "Development commands available:")
(log/info "  (start)              - Start server")
(log/info "  (stop)               - Stop server") 
(log/info "  (restart)            - Restart server")
(log/info "  (reset-db)           - Reset database")
(log/info "  (create-sample-slots) - Create test data")
(log/info "  (dev-setup)          - Full dev setup")
