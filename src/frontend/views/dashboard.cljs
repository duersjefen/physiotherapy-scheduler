(ns frontend.views.dashboard
  (:require [re-frame.core :as rf]))

(defn dashboard-stats []
  (let [patients @(rf/subscribe [:patients])
        appointments @(rf/subscribe [:appointments])
        todays-appointments @(rf/subscribe [:todays-appointments])
        this-week-sessions @(rf/subscribe [:this-week-sessions])]
    [:div.dashboard-stats
     [:div.stat-card
      [:div.stat-icon "📅"]
      [:div.stat-content
       [:h3 (str (count todays-appointments))]
       [:p "Today's Appointments"]]]
     
     [:div.stat-card
      [:div.stat-icon "👥"]
      [:div.stat-content
       [:h3 (str (count patients))]
       [:p "Total Patients"]]]
     
     [:div.stat-card
      [:div.stat-icon "📊"]
      [:div.stat-content
       [:h3 (str (count this-week-sessions))]
       [:p "This Week's Sessions"]]]
     
     [:div.stat-card
      [:div.stat-icon "⭐"]
      [:div.stat-content
       [:h3 "96%"]
       [:p "Patient Satisfaction"]]]]))

(defn upcoming-appointments []
  (let [appointments @(rf/subscribe [:upcoming-appointments])]
    [:div.upcoming-appointments
     [:h3 "Upcoming Appointments"]
     (if (empty? appointments)
       [:p.no-data "No upcoming appointments today"]
       [:div.appointments-list
        (for [appointment (take 5 appointments)]
          [:div.appointment-card {:key (:id appointment)}
           [:div.appointment-time
            [:span.time (or (:start-time appointment) "TBD")]
            [:span.date (or (:date appointment) "Today")]]
           [:div.appointment-details
            [:h4 (or (:patient-name appointment) "Unknown Patient")]
            [:p (or (:treatment-type appointment) "General Physiotherapy")]
            (when (:notes appointment)
              [:p.notes (:notes appointment)])]
           [:div.appointment-actions
            [:button.btn-small.btn-secondary 
             {:on-click #(rf/dispatch [:view-patient (:patient-id appointment)])}
             "View Patient"]
            [:button.btn-small.btn-primary
             {:on-click #(rf/dispatch [:start-session (:id appointment)])}
             "Start Session"]]])])]))

(defn recent-patients []
  (let [patients @(rf/subscribe [:recent-patients])]
    [:div.recent-patients
     [:h3 "Recent Patients"]
     (if (empty? patients)
       [:p.no-data "No recent patient activity"]
       [:div.patients-list
        (for [patient (take 5 patients)]
          [:div.patient-card {:key (:id patient)}
           [:div.patient-info
            [:h4 (:name patient)]
            [:p (:email patient)]
            [:p.last-visit "Last visit: " (or (:last-visit patient) "N/A")]]
           [:div.patient-actions
            [:button.btn-small.btn-secondary
             {:on-click #(rf/dispatch [:view-patient-history (:id patient)])}
             "View History"]
            [:button.btn-small.btn-primary
             {:on-click #(rf/dispatch [:schedule-appointment (:id patient)])}
             "Schedule"]]])])]))

(defn quick-actions []
  [:div.quick-actions
   [:h3 "Quick Actions"]
   [:div.actions-grid
    [:button.action-card
     {:on-click #(rf/dispatch [:set-current-page :appointments])}
     [:div.action-icon "📅"]
     [:span "Manage Appointments"]]
    
    [:button.action-card
     {:on-click #(rf/dispatch [:set-current-page :patients])}
     [:div.action-icon "👥"]
     [:span "Patient Records"]]
    
    [:button.action-card
     {:on-click #(rf/dispatch [:show-new-patient-modal])}
     [:div.action-icon "➕"]
     [:span "New Patient"]]
    
    [:button.action-card
     {:on-click #(rf/dispatch [:set-current-page :slots])}
     [:div.action-icon "🕒"]
     [:span "Available Slots"]]]])

(defn dashboard-header []
  (let [user @(rf/subscribe [:user])]
    [:div.dashboard-header
     [:h1 "Welcome back, " (or (:name user) "Doctor")]
     [:p.dashboard-subtitle "Here's what's happening in your practice today"]]))

(defn dashboard-page []
  (let [loading? @(rf/subscribe [:loading])]
    ;; Load data when component mounts
    (rf/dispatch [:load-dashboard-data])
    
    [:div.dashboard-page
     [:div.container
      [dashboard-header]
      [dashboard-stats]
      
      [:div.dashboard-content
       [:div.dashboard-main
        [upcoming-appointments]]
       
       [:div.dashboard-sidebar
        [recent-patients]
        [quick-actions]]]]]))

;; Legacy dashboard function for backward compatibility
(defn dashboard []
  [dashboard-page])
