(ns backend.middleware.error-handling
  (:require [taoensso.timbre :as log]
            [cheshire.core :as json]))

(defn wrap-error-handling [handler]
  (fn [request]
    (try
      (handler request)
      (catch clojure.lang.ExceptionInfo e
        (let [data (ex-data e)]
          (log/error "Application error:" (.getMessage e) data)
          (case (:type data)
            :validation-error
            {:status 400
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {:error "Validation failed"
                                        :details (:errors data)})}
            
            :not-found
            {:status 404
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {:error "Resource not found"})}
            
            :unauthorized
            {:status 401
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {:error "Unauthorized"})}
            
            :forbidden
            {:status 403
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {:error "Forbidden"})}
            
            ;; Default case
            {:status 500
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {:error "Internal server error"})})))
      
      (catch Exception e
        (log/error "Unexpected error:" (.getMessage e) (.getStackTrace e))
        {:status 500
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string {:error "Internal server error"})}))))
