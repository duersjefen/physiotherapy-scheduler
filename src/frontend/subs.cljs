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
 :slots
 (fn [db _]
   (:slots db)))

(rf/reg-sub
 :available-slots
 (fn [db _]
   (filter :slot/available (:slots db))))

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
