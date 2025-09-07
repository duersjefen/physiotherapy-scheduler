(ns frontend.views.patients
  (:require [re-frame.core :as rf]))

(defn patients-page []
  (let [patients @(rf/subscribe [:patients])]
    
    ;; Load patients on component mount
    (rf/dispatch [:load-patients])
    
    [:div.patients-page
     [:h1 "Patients"]
     [:div.patients-content
      [:div.patients-list
       [:h2 "Patient List"]
       (if (empty? patients)
         [:p "No patients found"]
         [:table.patients-table
          [:thead
           [:tr
            [:th "Name"]
            [:th "Email"]
            [:th "Phone"]
            [:th "Date of Birth"]
            [:th "Actions"]]]
          [:tbody
           (for [patient patients]
             ^{:key (:patient/id patient)}
             [:tr
              [:td (:patient/name patient)]
              [:td (:patient/email patient)]
              [:td (:patient/phone patient)]
              [:td (str (:patient/date-of-birth patient))]
              [:td
               [:button "Edit"]
               [:button "Delete"]]])]])]
      
      [:div.add-patient-form
       [:h2 "Add New Patient"]
       [:p "Patient form coming soon..."]]]]))
