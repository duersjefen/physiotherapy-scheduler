(ns frontend.utils.http
  (:require [ajax.core :as ajax]))

(defn api-request [method uri & [opts]]
  (merge {:method method
          :uri (str "http://localhost:8085" uri)
          :format (ajax/json-request-format)
          :response-format (ajax/json-response-format {:keywords? true})}
         opts))

(defn get-request [uri & [opts]]
  (api-request :get uri opts))

(defn post-request [uri data & [opts]]
  (api-request :post uri (merge {:params data} opts)))

(defn put-request [uri data & [opts]]
  (api-request :put uri (merge {:params data} opts)))

(defn delete-request [uri & [opts]]
  (api-request :delete uri opts))
