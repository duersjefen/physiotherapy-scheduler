(ns backend.config)

(defn server-port []
  (Integer/parseInt (or (System/getenv "PORT") "5000")))

(defn database-file []
  (or (System/getenv "DATABASE_FILE") "physiotherapy-dev.db"))

(defn database-uri []
  (str "jdbc:sqlite:" (database-file)))

(defn auth-secret []
  (or (System/getenv "AUTH_SECRET") "dev-secret-key-change-in-production"))

(defn session-timeout []
  (Integer/parseInt (or (System/getenv "SESSION_TIMEOUT") "60")))
