(ns frontend.views.admin
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn admin-stats []
  [:div.admin-stats
   [:div.stat-card
    [:div.stat-icon "📅"]
    [:div.stat-content
     [:div.stat-number @(rf/subscribe [:dashboard-stat :todays-appointments])]
     [:div.stat-label "Today's Appointments"]]]
   
   [:div.stat-card
    [:div.stat-icon "👥"]
    [:div.stat-content
     [:div.stat-number @(rf/subscribe [:dashboard-stat :total-patients])]
     [:div.stat-label "Total Patients"]]]
   
   [:div.stat-card
    [:div.stat-icon "📊"]
    [:div.stat-content
     [:div.stat-number @(rf/subscribe [:dashboard-stat :this-week])]
     [:div.stat-label "This Week's Sessions"]]]
   
   [:div.stat-card
    [:div.stat-icon "⭐"]
    [:div.stat-content
     [:div.stat-number @(rf/subscribe [:dashboard-stat :satisfaction])]
     [:div.stat-label "Satisfaction Rate"]]]])

(defn massage-bookings-overview []
  [:div.bookings-section
   [:h3 "Recent Massage Bookings"]
   (let [bookings @(rf/subscribe [:recent-massage-bookings])]
     (if (empty? bookings)
       [:div.empty-state
        [:div.empty-icon "💆‍♀️"]
        [:p "No massage bookings yet"]
        [:button.btn-primary
         {:on-click #(rf/dispatch [:set-admin-page :bookings])}
         "View All Bookings"]]
       [:div.bookings-list
        (for [booking (take 5 bookings)]
          ^{:key (:id booking)}
          [:div.booking-card
           [:div.booking-header
            [:div.customer-name (:name booking)]
            [:div.booking-date (:created-at booking)]]
           [:div.booking-details
            [:div.service-type (:service-type booking)]
            [:div.booking-status {:class (:status booking)} (:status booking)]]
           [:div.booking-actions
            [:button.btn-small.btn-secondary
             {:on-click #(rf/dispatch [:view-booking (:id booking)])}
             "View"]
            [:button.btn-small.btn-primary
             {:on-click #(rf/dispatch [:confirm-booking (:id booking)])}
             "Confirm"]]])
        [:div.view-all
         [:button.btn-secondary
          {:on-click #(rf/dispatch [:set-admin-page :bookings])}
          "View All Bookings"]]]))])

(defn schedule-management []
  [:div.schedule-section
   [:h3 "Schedule Management"]
   [:div.schedule-actions
    [:button.btn-primary
     {:on-click #(rf/dispatch [:show-create-slot-modal])}
     "Create Time Slot"]
    [:button.btn-secondary
     {:on-click #(rf/dispatch [:show-bulk-schedule-modal])}
     "Bulk Schedule Creation"]
    [:button.btn-secondary
     {:on-click #(rf/dispatch [:export-schedule])}
     "Export Schedule"]]
   
   [:div.schedule-overview
    [:h4 "This Week's Availability"]
    (let [slots @(rf/subscribe [:this-week-slots])]
      (if (empty? slots)
        [:div.empty-schedule
         [:p "No availability set for this week"]
         [:button.btn-primary
          {:on-click #(rf/dispatch [:quick-setup-week])}
          "Quick Setup This Week"]]
        [:div.slots-grid
         (for [slot slots]
           ^{:key (:id slot)}
           [:div.slot-card
            [:div.slot-time
             [:span.start-time (:start-time slot)]
             " - "
             [:span.end-time (:end-time slot)]]
            [:div.slot-status
             (if (:available slot)
               [:span.available "Available"]
               [:span.booked "Booked"])]
            [:div.slot-actions
             [:button.btn-small
              {:on-click #(rf/dispatch [:edit-slot (:id slot)])}
              "Edit"]
             [:button.btn-small.btn-danger
              {:on-click #(rf/dispatch [:delete-slot (:id slot)])}
              "Delete"]]])]))]])

(defn admin-navigation []
  [:div.admin-nav
   [:div.nav-item
    {:class (when (= @(rf/subscribe [:admin-page]) :dashboard) "active")
     :on-click #(rf/dispatch [:set-admin-page :dashboard])}
    [:div.nav-icon "🏠"]
    [:div.nav-label "Dashboard"]]
   
   [:div.nav-item
    {:class (when (= @(rf/subscribe [:admin-page]) :bookings) "active")
     :on-click #(rf/dispatch [:set-admin-page :bookings])}
    [:div.nav-icon "📋"]
    [:div.nav-label "Bookings"]]
   
   [:div.nav-item
    {:class (when (= @(rf/subscribe [:admin-page]) :schedule) "active")
     :on-click #(rf/dispatch [:set-admin-page :schedule])}
    [:div.nav-icon "📅"]
    [:div.nav-label "Schedule"]]
   
   [:div.nav-item
    {:class (when (= @(rf/subscribe [:admin-page]) :patients) "active")
     :on-click #(rf/dispatch [:set-admin-page :patients])}
    [:div.nav-icon "👥"]
    [:div.nav-label "Patients"]]
   
   [:div.nav-item
    {:class (when (= @(rf/subscribe [:admin-page]) :reports) "active")
     :on-click #(rf/dispatch [:set-admin-page :reports])}
    [:div.nav-icon "📊"]
    [:div.nav-label "Reports"]]
   
   [:div.nav-item
    {:class (when (= @(rf/subscribe [:admin-page]) :settings) "active")
     :on-click #(rf/dispatch [:set-admin-page :settings])}
    [:div.nav-icon "⚙️"]
    [:div.nav-label "Settings"]]])

(defn admin-dashboard []
  [:div.admin-dashboard
   [:h2 "Admin Dashboard"]
   [admin-stats]
   [massage-bookings-overview]
   [schedule-management]])

(defn bookings-page []
  [:div.bookings-page
   [:div.page-header
    [:h2 "Massage Bookings"]
    [:div.page-actions
     [:button.btn-secondary
      {:on-click #(rf/dispatch [:export-bookings])}
      "Export"]
     [:button.btn-primary
      {:on-click #(rf/dispatch [:refresh-bookings])}
      "Refresh"]]]
   
   [:div.bookings-filters
    [:div.filter-group
     [:label "Status"]
     [:select {:value @(rf/subscribe [:booking-filter :status])
               :on-change #(rf/dispatch [:set-booking-filter :status (-> % .-target .-value)])}
      [:option {:value ""} "All"]
      [:option {:value "pending"} "Pending"]
      [:option {:value "confirmed"} "Confirmed"]
      [:option {:value "completed"} "Completed"]
      [:option {:value "cancelled"} "Cancelled"]]]
    
    [:div.filter-group
     [:label "Service Type"]
     [:select {:value @(rf/subscribe [:booking-filter :service-type])
               :on-change #(rf/dispatch [:set-booking-filter :service-type (-> % .-target .-value)])}
      [:option {:value ""} "All Services"]
      [:option {:value "swedish"} "Swedish Massage"]
      [:option {:value "deep-tissue"} "Deep Tissue"]
      [:option {:value "sports"} "Sports Massage"]
      [:option {:value "hot-stone"} "Hot Stone"]
      [:option {:value "prenatal"} "Prenatal"]
      [:option {:value "aromatherapy"} "Aromatherapy"]]]
    
    [:div.filter-group
     [:label "Date Range"]
     [:input {:type "date"
              :value @(rf/subscribe [:booking-filter :from-date])
              :on-change #(rf/dispatch [:set-booking-filter :from-date (-> % .-target .-value)])}]
     [:span "to"]
     [:input {:type "date"
              :value @(rf/subscribe [:booking-filter :to-date])
              :on-change #(rf/dispatch [:set-booking-filter :to-date (-> % .-target .-value)])}]]]
   
   [:div.bookings-table
    (let [bookings @(rf/subscribe [:filtered-bookings])]
      (if (empty? bookings)
        [:div.empty-state
         [:div.empty-icon "📅"]
         [:p "No bookings found"]
         [:button.btn-primary
          {:on-click #(rf/dispatch [:clear-booking-filters])}
          "Clear Filters"]]
        [:table
         [:thead
          [:tr
           [:th "Customer"]
           [:th "Service"]
           [:th "Date Requested"]
           [:th "Status"]
           [:th "Actions"]]]
         [:tbody
          (for [booking bookings]
            ^{:key (:id booking)}
            [:tr
             [:td
              [:div.customer-info
               [:div.customer-name (:name booking)]
               [:div.customer-contact
                [:span (:email booking)]
                [:span (:phone booking)]]]]
             [:td (:service-type booking)]
             [:td (:created-at booking)]
             [:td
              [:span.status-badge {:class (:status booking)}
               (:status booking)]]
             [:td.actions
              [:button.btn-small.btn-secondary
               {:on-click #(rf/dispatch [:view-booking-details (:id booking)])}
               "View"]
              [:button.btn-small.btn-primary
               {:on-click #(rf/dispatch [:schedule-booking (:id booking)])}
               "Schedule"]
              [:button.btn-small.btn-danger
               {:on-click #(rf/dispatch [:cancel-booking (:id booking)])}
               "Cancel"]]])]]))]])

(defn admin-panel []
  [:div.admin-container
   [:div.admin-sidebar
    [admin-navigation]]
   
   [:div.admin-content
    (case @(rf/subscribe [:admin-page])
      :dashboard [admin-dashboard]
      :bookings [bookings-page]
      :schedule [:div "Schedule management coming soon"]
      :patients [:div "Patient management coming soon"]
      :reports [:div "Reports coming soon"]
      :settings [:div "Settings coming soon"]
      [admin-dashboard])]])
