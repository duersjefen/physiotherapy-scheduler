(ns user
  (:require [clojure.tools.namespace.repl :as repl]
            [backend.core :as core]
            [backend.config :as config]
            [backend.db.core :as db]))

(def server (atom nil))

(defn start []
  (when-not @server
    (reset! server (core/start-server))
    (println "Server started on port" (config/server-port))))

(defn stop []
  (when-let [s @server]
    (.stop s)
    (reset! server nil)
    (println "Server stopped")))

(defn restart []
  (stop)
  (repl/refresh :after 'user/start))

(defn reset-db []
  (db/init-db!)
  (println "Database reset"))

(println "Development environment loaded.")
(println "Available commands:")
(println "  (start)    - Start the server")
(println "  (stop)     - Stop the server") 
(println "  (restart)  - Restart the server")
(println "  (reset-db) - Reset the database")
