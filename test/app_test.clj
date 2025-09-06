(ns app-test
  "Basic tests for the physiotherapy scheduler"
  (:require [clojure.test :refer :all]
            [app.db.core :as db]
            [app.db.slots :as slots]))

(deftest database-connection-test
  (testing "Database connection works"
    (let [result (db/health-check)]
      (is (= (:status result) :healthy)))))

(deftest slot-creation-test
  (testing "Can create appointment slots"
    (let [slot (slots/create-slot! "2024-12-20T10:00:00Z" 60)]
      (is (some? slot))
      (is (= (:duration_minutes slot) 60)))))

(deftest slot-retrieval-test
  (testing "Can retrieve appointment slots"
    (let [slots (slots/get-available-slots "2024-12-01T00:00:00Z" "2024-12-31T23:59:59Z")]
      (is (vector? slots)))))

;; Run tests
(defn run-tests []
  (println "Running tests...")
  (run-all-tests))
