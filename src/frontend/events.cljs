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
       (assoc :current-page (if (= (:role (:user response)) "admin") :admin :dashboard)))))

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
       (assoc :current-page :home))))

;; Form handling
(rf/reg-event-db
 :update-form
 (fn [db [_ form-key field value]]
   (assoc-in db [:forms form-key field] value)))

;; Appointment booking
(rf/reg-event-fx
 :submit-appointment-request
 (fn [{:keys [db]} _]
   (let [form-data (get-in db [:forms :booking])]
     {:db (assoc db :loading true :error nil)
      :http-xhrio (http/post-request "/api/v1/appointment-requests" 
                                     form-data
                                     {:on-success [:appointment-request-success]
                                      :on-failure [:appointment-request-failure]})})))

;; New booking system
(rf/reg-event-db
 :set-booking-step
 (fn [db [_ step]]
   (assoc db :booking-step step)))

(rf/reg-event-db
 :next-booking-step
 (fn [db _]
   (let [current-step (get db :booking-step 1)]
     (if (< current-step 3)
       (assoc db :booking-step (inc current-step))
       db))))

(rf/reg-event-db
 :prev-booking-step
 (fn [db _]
   (let [current-step (get db :booking-step 1)]
     (if (> current-step 1)
       (assoc db :booking-step (dec current-step))
       db))))

(rf/reg-event-db
 :select-service
 (fn [db [_ service-type]]
   (assoc-in db [:booking :selected-service] service-type)))

(rf/reg-event-fx
 :submit-massage-booking
 (fn [{:keys [db]} _]
   (let [booking-data (merge 
                       (get-in db [:forms :booking])
                       {:service-type (get-in db [:booking :selected-service])
                        :booking-type "massage"})]
     {:db (assoc db :loading true :error nil)
      :http-xhrio (http/post-request "/api/v1/massage-bookings" 
                                     booking-data
                                     {:on-success [:massage-booking-success]
                                      :on-failure [:massage-booking-failure]})})))

(rf/reg-event-db
 :massage-booking-success
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc-in [:forms :booking] {})
       (assoc-in [:booking :selected-service] nil)
       (assoc :booking-step 1)
       (assoc :success-message "Massage booked successfully! You'll receive a confirmation email with calendar details."))))

(rf/reg-event-db
 :massage-booking-failure
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc :error "Failed to book massage. Please try again."))))

(rf/reg-event-db
 :appointment-request-success
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc-in [:forms :booking] {})
       (assoc :success-message "Appointment request submitted! We'll contact you soon to confirm."))))

(rf/reg-event-db
 :appointment-request-failure
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc :error "Failed to submit appointment request. Please try again."))))

;; Dashboard data loading
(rf/reg-event-fx
 :load-dashboard-data
 (fn [{:keys [db]} _]
   {:db (assoc db :loading true)
    :dispatch-n [[:load-patients]
                 [:load-appointments]
                 [:load-slots]
                 [:load-upcoming-appointments]
                 [:load-recent-patients]]}))

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

(rf/reg-event-fx
 :load-upcoming-appointments
 (fn [{:keys [db]} _]
   {:http-xhrio {:method :get
                 :uri "/api/v1/appointments/upcoming"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:load-upcoming-appointments-success]
                 :on-failure [:load-appointments-failure]}}))

(rf/reg-event-db
 :load-upcoming-appointments-success
 (fn [db [_ response]]
   (assoc db :upcoming-appointments (:appointments response))))

(rf/reg-event-fx
 :load-recent-patients
 (fn [{:keys [db]} _]
   {:http-xhrio {:method :get
                 :uri "/api/v1/patients/recent"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:load-recent-patients-success]
                 :on-failure [:load-patients-failure]}}))

(rf/reg-event-db
 :load-recent-patients-success
 (fn [db [_ response]]
   (assoc db :recent-patients (:patients response))))

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

(rf/reg-event-db
 :load-slots-failure
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc :error "Failed to load slots"))))

;; Admin navigation
(rf/reg-event-db
 :set-admin-page
 (fn [db [_ page]]
   (assoc db :admin-page page)))

;; Patient management
(rf/reg-event-db
 :show-new-patient-modal
 (fn [db _]
   (assoc db :show-new-patient-modal true)))

(rf/reg-event-db
 :hide-new-patient-modal
 (fn [db _]
   (assoc db :show-new-patient-modal false)))

;; Session management
(rf/reg-event-fx
 :start-session
 (fn [{:keys [db]} [_ appointment-id]]
   {:db (assoc db :current-session appointment-id)
    :dispatch [:set-current-page :session]}))

(rf/reg-event-fx
 :view-patient
 (fn [{:keys [db]} [_ patient-id]]
   {:db (assoc db :selected-patient-id patient-id)
    :dispatch [:set-current-page :patient-detail]}))

(rf/reg-event-fx
 :schedule-appointment
 (fn [{:keys [db]} [_ patient-id]]
   {:db (assoc db :schedule-patient-id patient-id)
    :dispatch [:set-current-page :schedule-appointment]}))
