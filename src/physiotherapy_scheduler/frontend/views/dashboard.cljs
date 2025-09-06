(ns physiotherapy-scheduler.frontend.views.dashboard
  (:require [re-frame.core :as rf]))

(defn stats-card [title value]
  [:div.stats-card
   [:h3 title]
   [:p.stats-value value]])

(defn dashboard []
  (let [patients @(rf/subscribe [:patients])
        appointments @(rf/subscribe [:appointments])
        slots @(rf/subscribe [:slots])
        available-slots @(rf/subscribe [:available-slots])]
    
    ;; Load data on component mount
    (rf/dispatch [:load-patients])
    (rf/dispatch [:load-appointments])
    (rf/dispatch [:load-slots])
    
    [:div.dashboard
     [:h1 "Dashboard"]
     [:div.stats-grid
      [stats-card "Total Patients" (count patients)]
      [stats-card "Total Appointments" (count appointments)]
      [stats-card "Available Slots" (count available-slots)]
      [stats-card "Total Slots" (count slots)]]
     
     [:div.dashboard-content
      [:div.recent-appointments
       [:h2 "Recent Appointments"]
       (if (empty? appointments)
         [:p "No appointments found"]
         [:div.appointments-list
          (for [appointment (take 5 appointments)]
            ^{:key (:appointment/id appointment)}
            [:div.appointment-card
             [:p "Patient: " (get-in appointment [:appointment/patient :patient/name])]
             [:p "Time: " (str (get-in appointment [:appointment/slot :slot/start-time]))]
             [:p "Status: " (name (:appointment/status appointment))]])])]
      
      [:div.quick-actions
       [:h2 "Quick Actions"]
       [:div.action-buttons
        [:button {:on-click #(rf/dispatch [:set-current-page :patients])}
         "Manage Patients"]
        [:button {:on-click #(rf/dispatch [:set-current-page :appointments])}
         "Schedule Appointment"]
        [:button {:on-click #(rf/dispatch [:set-current-page :slots])}
         "Manage Slots"]]]]]))
