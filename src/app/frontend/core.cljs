(ns app.frontend.core
  "Frontend application using Reagent and re-frame"
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [clojure.string :as str]))

;; ============================================================================
;; Events
;; ============================================================================

;; Initialize app state
(rf/reg-event-db
 :initialize-db
 (fn [_ _]
   {:current-view :public
    :loading false
    :error nil
    :available-slots []
    :admin-slots []
    :booking-form {:client-name ""
                   :client-email ""}
    :create-slot-form {:start-time ""
                       :duration 60}
    :selected-date-range {:start "2024-12-01"
                          :end "2024-12-31"}}))

;; Navigation
(rf/reg-event-db
 :set-view
 (fn [db [_ view]]
   (assoc db :current-view view)))

;; Loading state
(rf/reg-event-db
 :set-loading
 (fn [db [_ loading]]
   (assoc db :loading loading)))

;; Error handling
(rf/reg-event-db
 :set-error
 (fn [db [_ error]]
   (assoc db :error error)))

(rf/reg-event-db
 :clear-error
 (fn [db _]
   (assoc db :error nil)))

;; Fetch available slots
(rf/reg-event-fx
 :fetch-available-slots
 (fn [{:keys [db]} _]
   (let [{:keys [start end]} (:selected-date-range db)]
     {:db (assoc db :loading true)
      :http-xhrio {:method :get
                   :uri (str "/api/slots/available?start-date=" start "T00:00:00Z&end-date=" end "T23:59:59Z")
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:fetch-available-slots-success]
                   :on-failure [:fetch-available-slots-failure]}})))

(rf/reg-event-db
 :fetch-available-slots-success
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc :available-slots (:slots response))
       (assoc :error nil))))

(rf/reg-event-db
 :fetch-available-slots-failure
 (fn [db [_ error]]
   (-> db
       (assoc :loading false)
       (assoc :error "Failed to load available slots"))))

;; Book slot
(rf/reg-event-fx
 :book-slot
 (fn [{:keys [db]} [_ slot-id]]
   (let [{:keys [client-name client-email]} (:booking-form db)]
     {:http-xhrio {:method :post
                   :uri (str "/api/slots/" slot-id "/book")
                   :params {:client-name client-name
                            :client-email client-email}
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:book-slot-success]
                   :on-failure [:book-slot-failure]}})))

(rf/reg-event-fx
 :book-slot-success
 (fn [{:keys [db]} [_ response]]
   {:db (-> db
            (assoc :booking-form {:client-name "" :client-email ""})
            (assoc :error nil))
    :dispatch [:fetch-available-slots]
    :alert "Appointment booked successfully!"}))

(rf/reg-event-db
 :book-slot-failure
 (fn [db [_ error]]
   (let [error-msg (get-in error [:response :error] "Failed to book appointment")]
     (assoc db :error error-msg))))

;; Update forms
(rf/reg-event-db
 :update-booking-form
 (fn [db [_ field value]]
   (assoc-in db [:booking-form field] value)))

(rf/reg-event-db
 :update-create-slot-form
 (fn [db [_ field value]]
   (assoc-in db [:create-slot-form field] value)))

(rf/reg-event-db
 :update-date-range
 (fn [db [_ field value]]
   (assoc-in db [:selected-date-range field] value)))

;; Create slot (admin)
(rf/reg-event-fx
 :create-slot
 (fn [{:keys [db]} _]
   (let [{:keys [start-time duration]} (:create-slot-form db)]
     {:http-xhrio {:method :post
                   :uri "/api/slots"
                   :params {:start-time start-time
                            :duration duration}
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:create-slot-success]
                   :on-failure [:create-slot-failure]}})))

(rf/reg-event-fx
 :create-slot-success
 (fn [{:keys [db]} [_ response]]
   {:db (-> db
            (assoc :create-slot-form {:start-time "" :duration 60})
            (assoc :error nil))
    :dispatch [:fetch-admin-slots]
    :alert "Slot created successfully!"}))

(rf/reg-event-db
 :create-slot-failure
 (fn [db [_ error]]
   (let [error-msg (get-in error [:response :error] "Failed to create slot")]
     (assoc db :error error-msg))))

;; Fetch admin slots
(rf/reg-event-fx
 :fetch-admin-slots
 (fn [{:keys [db]} _]
   {:db (assoc db :loading true)
    :http-xhrio {:method :get
                 :uri "/api/admin/slots"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:fetch-admin-slots-success]
                 :on-failure [:fetch-admin-slots-failure]}}))

(rf/reg-event-db
 :fetch-admin-slots-success
 (fn [db [_ response]]
   (-> db
       (assoc :loading false)
       (assoc :admin-slots (:slots response))
       (assoc :error nil))))

(rf/reg-event-db
 :fetch-admin-slots-failure
 (fn [db [_ error]]
   (-> db
       (assoc :loading false)
       (assoc :error "Failed to load admin slots"))))

;; ============================================================================
;; Subscriptions
;; ============================================================================

(rf/reg-sub
 :current-view
 (fn [db _]
   (:current-view db)))

(rf/reg-sub
 :loading
 (fn [db _]
   (:loading db)))

(rf/reg-sub
 :error
 (fn [db _]
   (:error db)))

(rf/reg-sub
 :available-slots
 (fn [db _]
   (:available-slots db)))

(rf/reg-sub
 :admin-slots
 (fn [db _]
   (:admin-slots db)))

(rf/reg-sub
 :booking-form
 (fn [db _]
   (:booking-form db)))

(rf/reg-sub
 :create-slot-form
 (fn [db _]
   (:create-slot-form db)))

(rf/reg-sub
 :selected-date-range
 (fn [db _]
   (:selected-date-range db)))

;; ============================================================================
;; Components
;; ============================================================================

(defn format-datetime
  "Format ISO8601 timestamp for display"
  [iso-timestamp]
  (when iso-timestamp
    (let [date (js/Date. iso-timestamp)]
      (.toLocaleString date))))

(defn error-banner
  "Error message banner"
  []
  (when-let [error @(rf/subscribe [:error])]
    [:div.bg-red-100.border.border-red-400.text-red-700.px-4.py-3.rounded.mb-4
     {:role "alert"}
     [:span.block.sm:inline error]
     [:button.float-right.px-2.py-1.text-red-500.hover:text-red-700
      {:on-click #(rf/dispatch [:clear-error])}
      "×"]]))

(defn loading-spinner
  "Loading spinner component"
  []
  [:div.flex.justify-center.items-center.py-8
   [:div.animate-spin.rounded-full.h-8.w-8.border-b-2.border-blue-500]])

(defn navigation
  "Navigation component"
  []
  (let [current-view @(rf/subscribe [:current-view])]
    [:nav.bg-blue-600.text-white.p-4.mb-6
     [:div.container.mx-auto.flex.justify-between.items-center
      [:h1.text-xl.font-bold "Physiotherapy Scheduler"]
      [:div.space-x-4
       [:button.px-4.py-2.rounded.hover:bg-blue-700
        {:class (when (= current-view :public) "bg-blue-800")
         :on-click #(rf/dispatch [:set-view :public])}
        "Book Appointment"]
       [:button.px-4.py-2.rounded.hover:bg-blue-700
        {:class (when (= current-view :admin) "bg-blue-800")
         :on-click #(rf/dispatch [:set-view :admin])}
        "Admin"]]]]))

(defn booking-form
  "Booking form component"
  [slot-id]
  (let [form @(rf/subscribe [:booking-form])]
    [:div.mt-4.p-4.border.rounded.bg-gray-50
     [:h4.font-semibold.mb-4 "Book this appointment"]
     [:div.space-y-4
      [:div
       [:label.block.text-sm.font-medium.text-gray-700 {:for "client-name"} "Your Name"]
       [:input.mt-1.block.w-full.border.border-gray-300.rounded-md.px-3.py-2.focus:outline-none.focus:ring-2.focus:ring-blue-500
        {:id "client-name"
         :type "text"
         :value (:client-name form)
         :on-change #(rf/dispatch [:update-booking-form :client-name (-> % .-target .-value)])
         :required true}]]
      [:div
       [:label.block.text-sm.font-medium.text-gray-700 {:for "client-email"} "Email Address"]
       [:input.mt-1.block.w-full.border.border-gray-300.rounded-md.px-3.py-2.focus:outline-none.focus:ring-2.focus:ring-blue-500
        {:id "client-email"
         :type "email"
         :value (:client-email form)
         :on-change #(rf/dispatch [:update-booking-form :client-email (-> % .-target .-value)])
         :required true}]]
      [:div.flex.space-x-2
       [:button.px-4.py-2.bg-blue-600.text-white.rounded.hover:bg-blue-700.focus:outline-none.focus:ring-2.focus:ring-blue-500
        {:on-click #(rf/dispatch [:book-slot slot-id])}
        "Confirm Booking"]
       [:button.px-4.py-2.bg-gray-300.text-gray-700.rounded.hover:bg-gray-400.focus:outline-none.focus:ring-2.focus:ring-gray-500
        {:on-click #(rf/dispatch [:update-booking-form :show-form false])}
        "Cancel"]]]]))

(defn slot-card
  "Individual slot card component"
  [slot]
  (let [form @(rf/subscribe [:booking-form])]
    [:div.border.rounded.p-4.mb-4.bg-white.shadow-sm
     [:div.flex.justify-between.items-start
      [:div
       [:p.font-semibold (format-datetime (:start_time slot))]
       [:p.text-gray-600 (str (:duration_minutes slot) " minutes")]]
      [:button.px-4.py-2.bg-green-600.text-white.rounded.hover:bg-green-700.focus:outline-none.focus:ring-2.focus:ring-green-500
       {:on-click #(rf/dispatch [:update-booking-form :show-form (:id slot)])}
       "Book"]]
     (when (= (:show-form form) (:id slot))
       [booking-form (:id slot)])]))

(defn date-range-picker
  "Date range picker for filtering slots"
  []
  (let [date-range @(rf/subscribe [:selected-date-range])]
    [:div.mb-6.p-4.border.rounded.bg-gray-50
     [:h3.font-semibold.mb-4 "Select Date Range"]
     [:div.flex.space-x-4.items-end
      [:div
       [:label.block.text-sm.font-medium.text-gray-700 {:for "start-date"} "Start Date"]
       [:input.mt-1.block.border.border-gray-300.rounded-md.px-3.py-2.focus:outline-none.focus:ring-2.focus:ring-blue-500
        {:id "start-date"
         :type "date"
         :value (:start date-range)
         :on-change #(rf/dispatch [:update-date-range :start (-> % .-target .-value)])}]]
      [:div
       [:label.block.text-sm.font-medium.text-gray-700 {:for "end-date"} "End Date"]
       [:input.mt-1.block.border.border-gray-300.rounded-md.px-3.py-2.focus:outline-none.focus:ring-2.focus:ring-blue-500
        {:id "end-date"
         :type "date"
         :value (:end date-range)
         :on-change #(rf/dispatch [:update-date-range :end (-> % .-target .-value)])}]]
      [:button.px-4.py-2.bg-blue-600.text-white.rounded.hover:bg-blue-700.focus:outline-none.focus:ring-2.focus:ring-blue-500
       {:on-click #(rf/dispatch [:fetch-available-slots])}
       "Search"]]]))

(defn public-view
  "Public appointment booking view"
  []
  (let [slots @(rf/subscribe [:available-slots])
        loading @(rf/subscribe [:loading])]
    [:div.container.mx-auto.px-4
     [:h2.text-2xl.font-bold.mb-6 "Available Appointments"]
     [date-range-picker]
     (cond
       loading [loading-spinner]
       (empty? slots) [:p.text-gray-600.text-center.py-8 "No available appointments in the selected date range."]
       :else [:div.grid.gap-4
              (for [slot slots]
                ^{:key (:id slot)}
                [slot-card slot])])]))

(defn create-slot-form
  "Form for creating new appointment slots (admin)"
  []
  (let [form @(rf/subscribe [:create-slot-form])]
    [:div.mb-8.p-6.border.rounded.bg-gray-50
     [:h3.text-lg.font-semibold.mb-4 "Create New Appointment Slot"]
     [:div.space-y-4
      [:div
       [:label.block.text-sm.font-medium.text-gray-700 {:for "slot-datetime"} "Date & Time"]
       [:input.mt-1.block.w-full.border.border-gray-300.rounded-md.px-3.py-2.focus:outline-none.focus:ring-2.focus:ring-blue-500
        {:id "slot-datetime"
         :type "datetime-local"
         :value (:start-time form)
         :on-change #(rf/dispatch [:update-create-slot-form :start-time (str (-> % .-target .-value) ":00Z")])}]]
      [:div
       [:label.block.text-sm.font-medium.text-gray-700 {:for "slot-duration"} "Duration (minutes)"]
       [:input.mt-1.block.w-full.border.border-gray-300.rounded-md.px-3.py-2.focus:outline-none.focus:ring-2.focus:ring-blue-500
        {:id "slot-duration"
         :type "number"
         :min "15"
         :max "180"
         :value (:duration form)
         :on-change #(rf/dispatch [:update-create-slot-form :duration (js/parseInt (-> % .-target .-value))])}]]
      [:button.px-6.py-2.bg-blue-600.text-white.rounded.hover:bg-blue-700.focus:outline-none.focus:ring-2.focus:ring-blue-500
       {:on-click #(rf/dispatch [:create-slot])}
       "Create Slot"]]]))

(defn admin-slot-row
  "Admin slot table row"
  [slot]
  [:tr.hover:bg-gray-50
   [:td.px-4.py-2 (:id slot)]
   [:td.px-4.py-2 (format-datetime (:start_time slot))]
   [:td.px-4.py-2 (str (:duration_minutes slot) " min")]
   [:td.px-4.py-2
    [:span.px-2.py-1.rounded.text-sm
     {:class (case (:status slot)
               "available" "bg-green-100 text-green-800"
               "booked" "bg-blue-100 text-blue-800"
               "cancelled" "bg-red-100 text-red-800")}
     (:status slot)]]
   [:td.px-4.py-2 (or (:client_name slot) "-")]
   [:td.px-4.py-2 (or (:client_email slot) "-")]])

(defn admin-view
  "Admin view for managing appointments"
  []
  (let [slots @(rf/subscribe [:admin-slots])
        loading @(rf/subscribe [:loading])]
    [:div.container.mx-auto.px-4
     [:h2.text-2xl.font-bold.mb-6 "Admin Panel"]
     [create-slot-form]
     [:div.mb-4.flex.justify-between.items-center
      [:h3.text-lg.font-semibold "All Appointment Slots"]
      [:button.px-4.py-2.bg-blue-600.text-white.rounded.hover:bg-blue-700.focus:outline-none.focus:ring-2.focus:ring-blue-500
       {:on-click #(rf/dispatch [:fetch-admin-slots])}
       "Refresh"]]
     (if loading
       [loading-spinner]
       [:div.overflow-x-auto
        [:table.min-w-full.bg-white.border.border-gray-200
         [:thead.bg-gray-50
          [:tr
           [:th.px-4.py-2.text-left.text-sm.font-medium.text-gray-700 "ID"]
           [:th.px-4.py-2.text-left.text-sm.font-medium.text-gray-700 "Start Time"]
           [:th.px-4.py-2.text-left.text-sm.font-medium.text-gray-700 "Duration"]
           [:th.px-4.py-2.text-left.text-sm.font-medium.text-gray-700 "Status"]
           [:th.px-4.py-2.text-left.text-sm.font-medium.text-gray-700 "Client Name"]
           [:th.px-4.py-2.text-left.text-sm.font-medium.text-gray-700 "Client Email"]]]
         [:tbody
          (for [slot slots]
            ^{:key (:id slot)}
            [admin-slot-row slot])]]])]))

(defn main-app
  "Main application component"
  []
  (let [current-view @(rf/subscribe [:current-view])]
    [:div.min-h-screen.bg-gray-100
     [navigation]
     [error-banner]
     (case current-view
       :public [public-view]
       :admin [admin-view]
       [public-view])]))

;; ============================================================================
;; App Initialization
;; ============================================================================

(defn init
  "Initialize the application"
  []
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch [:fetch-available-slots])
  (rdom/render [main-app] (.getElementById js/document "app")))

;; Start the app when page loads
(defn ^:export start []
  (init))

;; For hot reloading
(defn ^:dev/after-load reload []
  (init))
