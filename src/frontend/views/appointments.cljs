(ns frontend.views.appointments
  (:require [re-frame.core :as rf]))

(defn appointments-page []
  (let [appointments @(rf/subscribe [:appointments])]
    
    ;; Load appointments on component mount
    (rf/dispatch [:load-appointments])
    
    [:div.appointments-page
     [:h1 "Appointments"]
     [:div.appointments-content
      [:div.appointments-list
       [:h2 "Appointment List"]
       (if (empty? appointments)
         [:p "No appointments found"]
         [:table.appointments-table
          [:thead
           [:tr
            [:th "Patient"]
            [:th "Date & Time"]
            [:th "Status"]
            [:th "Notes"]
            [:th "Actions"]]]
          [:tbody
           (for [appointment appointments]
             ^{:key (:appointment/id appointment)}
             [:tr
              [:td (get-in appointment [:appointment/patient :patient/name])]
              [:td (str (get-in appointment [:appointment/slot :slot/start-time]))]
              [:td (name (:appointment/status appointment))]
              [:td (:appointment/notes appointment)]
              [:td
               [:button "Edit"]
               [:button "Cancel"]]])]])]
      
      [:div.add-appointment-form
       [:h2 "Schedule New Appointment"]
       [:p "Appointment form coming soon..."]]]]))
