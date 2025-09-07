(ns frontend.events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [frontend.db :as db]
            [frontend.utils.http :as http]))

;; Initialize
(rf/reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))

;; Navigation
(rf/reg-event-db
 :set-current-page
 (fn [db [_ page]]
   (assoc db :current-page page)))

;; Loading
(rf/reg-event-db
 :set-loading
 (fn [db [_ loading?]]
   (assoc db :loading loading?)))

;; Error handling
(rf/reg-event-db
 :set-error
 (fn [db [_ error]]
   (assoc db :error error)))

(rf/reg-event-db
 :clear-error
 (fn [db _]
   (assoc db :error nil)))

;; Authentication
(rf/reg-event-fx
 :login
 (fn [{:keys [db]} [_ credentials]]
   {:db (assoc db :loading true :error nil)
    :http-xhrio {:method :post
                 :uri "/api/auth/login"
                 :params credentials
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:login-success]
                 :on-failure [:login-failure]}}))

(rf/reg-event-db
 :login-success
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc :user (:user response))
       (assoc :current-page :dashboard))))

(rf/reg-event-db
 :login-failure
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc :error "Login failed"))))

(rf/reg-event-fx
 :logout
 (fn [{:keys [db]} _]
   {:db (assoc db :loading true)
    :http-xhrio {:method :post
                 :uri "/api/auth/logout"
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:logout-success]
                 :on-failure [:logout-failure]}}))

(rf/reg-event-db
 :logout-success
 (fn [db _]
   (-> db
       (assoc :loading false)
       (assoc :user nil)
       (assoc :current-page :login))))

;; Form handling
(rf/reg-event-db
 :update-form
 (fn [db [_ form-key field value]]
   (assoc-in db [:forms form-key field] value)))

;; Patients
(rf/reg-event-fx
 :load-patients
 (fn [{:keys [db]} _]
   {:db (assoc db :loading true)
    :http-xhrio {:method :get
                 :uri "/api/v1/patients"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:load-patients-success]
                 :on-failure [:load-patients-failure]}}))

(rf/reg-event-db
 :load-patients-success
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc :patients (:patients response)))))

(rf/reg-event-db
 :load-patients-failure
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc :error "Failed to load patients"))))

;; Appointments
(rf/reg-event-fx
 :load-appointments
 (fn [{:keys [db]} _]
   {:db (assoc db :loading true)
    :http-xhrio {:method :get
                 :uri "/api/v1/appointments"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:load-appointments-success]
                 :on-failure [:load-appointments-failure]}}))

(rf/reg-event-db
 :load-appointments-success
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc :appointments (:appointments response)))))

;; Slots
(rf/reg-event-fx
 :load-slots
 (fn [{:keys [db]} _]
   {:db (assoc db :loading true)
    :http-xhrio {:method :get
                 :uri "/api/v1/slots"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:load-slots-success]
                 :on-failure [:load-slots-failure]}}))

(rf/reg-event-db
 :load-slots-success
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc :slots (:slots response)))))

;; Admin navigation
(rf/reg-event-db
 :set-admin-page
 (fn [db [_ page]]
   (assoc db :admin-page page)))
