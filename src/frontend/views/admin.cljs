(ns frontend.views.admin
  (:require [re-frame.core :as rf]
            [frontend.views.patients :as patients]
            [frontend.views.appointments :as appointments]
            [frontend.views.slots :as slots]))

(defn admin-navigation []
  [:div.admin-nav
   [:h2 "Admin Panel"]
   [:div.admin-nav-links
    [:a {:class (when (= @(rf/subscribe [:admin-page]) :dashboard) "active")
         :on-click #(rf/dispatch [:set-admin-page :dashboard])}
     "Dashboard"]
    [:a {:class (when (= @(rf/subscribe [:admin-page]) :patients) "active")
         :on-click #(rf/dispatch [:set-admin-page :patients])}
     "Patients"]
    [:a {:class (when (= @(rf/subscribe [:admin-page]) :appointments) "active")
         :on-click #(rf/dispatch [:set-admin-page :appointments])}
     "Appointments"]
    [:a {:class (when (= @(rf/subscribe [:admin-page]) :slots) "active")
         :on-click #(rf/dispatch [:set-admin-page :slots])}
     "Time Slots"]]])

(defn admin-dashboard []
  [:div.admin-dashboard
   [:h1 "Dashboard"]
   [:div.stats-grid
    [:div.stats-card
     [:div.stats-value @(rf/subscribe [:total-patients])]
     [:div.stats-label "Total Patients"]]
    [:div.stats-card
     [:div.stats-value @(rf/subscribe [:total-appointments])]
     [:div.stats-label "Total Appointments"]]
    [:div.stats-card
     [:div.stats-value @(rf/subscribe [:available-slots])]
     [:div.stats-label "Available Slots"]]
    [:div.stats-card
     [:div.stats-value @(rf/subscribe [:today-appointments])]
     [:div.stats-label "Today's Appointments"]]]])

(defn admin-panel []
  (let [admin-page @(rf/subscribe [:admin-page])]
    [:div.admin-panel
     [admin-navigation]
     [:div.admin-content
      (case admin-page
        :dashboard [admin-dashboard]
        :patients [patients/patients-page]
        :appointments [appointments/appointments-page]
        :slots [slots/slots-page]
        [admin-dashboard])]]))
