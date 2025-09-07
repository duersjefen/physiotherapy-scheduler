(ns frontend.subs
  (:require [re-frame.core :as rf]))

;; Basic subscriptions
(rf/reg-sub
 :current-page
 (fn [db _]
   (:current-page db)))

(rf/reg-sub
 :loading
 (fn [db _]
   (:loading db)))

(rf/reg-sub
 :error
 (fn [db _]
   (:error db)))

(rf/reg-sub
 :success-message
 (fn [db _]
   (:success-message db)))

(rf/reg-sub
 :user
 (fn [db _]
   (:user db)))

(rf/reg-sub
 :authenticated?
 (fn [db _]
   (some? (:user db))))

;; Form subscriptions
(rf/reg-sub
 :form
 (fn [db [_ form-key]]
   (get-in db [:forms form-key])))

(rf/reg-sub
 :form-value
 (fn [db [_ form-key field]]
   (get-in db [:forms form-key field])))

;; Data subscriptions
(rf/reg-sub
 :patients
 (fn [db _]
   (:patients db)))

(rf/reg-sub
 :appointments
 (fn [db _]
   (:appointments db)))

(rf/reg-sub
 :upcoming-appointments
 (fn [db _]
   (:upcoming-appointments db)))

(rf/reg-sub
 :recent-patients
 (fn [db _]
   (:recent-patients db)))

(rf/reg-sub
 :slots
 (fn [db _]
   (:slots db)))

(rf/reg-sub
 :available-slots
 (fn [db _]
   (filter :slot/available (:slots db))))

;; Dashboard subscriptions
(rf/reg-sub
 :todays-appointments
 (fn [db _]
   ;; Filter appointments for today - simplified for demo
   (filter #(= (:date %) "today") (:upcoming-appointments db))))

(rf/reg-sub
 :this-week-sessions
 (fn [db _]
   ;; Filter sessions for this week - simplified for demo
   (filter #(= (:week %) "current") (:appointments db))))

;; Admin subscriptions
(rf/reg-sub
 :admin-page
 (fn [db _]
   (:admin-page db)))

(rf/reg-sub
 :total-patients
 (fn [db _]
   (count (:patients db))))

(rf/reg-sub
 :total-appointments
 (fn [db _]
   (count (:appointments db))))

(rf/reg-sub
 :today-appointments
 (fn [db _]
   ;; This would need proper date filtering in a real app
   (count (:appointments db))))

;; Modal subscriptions
(rf/reg-sub
 :show-new-patient-modal
 (fn [db _]
   (:show-new-patient-modal db)))

;; Dashboard stats helper
(rf/reg-sub
 :dashboard-stat
 (fn [db [_ stat-key]]
   (case stat-key
     :todays-appointments (count (:upcoming-appointments db))
     :total-patients (count (:patients db))
     :this-week (count (:appointments db))
     :satisfaction "96%"
     0)))

;; Booking system subscriptions
(rf/reg-sub
 :booking-step
 (fn [db _]
   (get db :booking-step 1)))

(rf/reg-sub
 :booking-step-active?
 (fn [db [_ step]]
   (= (get db :booking-step 1) step)))

(rf/reg-sub
 :selected-service
 (fn [db _]
   (get-in db [:booking :selected-service])))

(rf/reg-sub
 :selected-service-is?
 (fn [db [_ service-type]]
   (= (get-in db [:booking :selected-service]) service-type)))

(rf/reg-sub
 :has-selected-service?
 (fn [db _]
   (some? (get-in db [:booking :selected-service]))))
