(ns backend.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]))

(def ^:private config-cache (atom nil))

(defn load-config
  ([]
   (load-config :dev))
  ([profile]
   (if-let [cached @config-cache]
     cached
     (let [config (aero/read-config (io/resource "config.edn") {:profile profile})]
       (reset! config-cache config)
       config))))

(defn get-config [& keys]
  (get-in (load-config) keys))

(defn server-port []
  (get-config :server :port))

(defn database-uri []
  (get-config :database :uri))

(defn auth-secret []
  (get-config :auth :secret))

(defn session-timeout []
  (get-config :auth :session-timeout-minutes))
