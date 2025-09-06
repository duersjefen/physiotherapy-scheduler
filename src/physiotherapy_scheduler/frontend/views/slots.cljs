(ns physiotherapy-scheduler.frontend.views.slots
  (:require [re-frame.core :as rf]))

(defn slots-page []
  (let [slots @(rf/subscribe [:slots])]
    
    ;; Load slots on component mount
    (rf/dispatch [:load-slots])
    
    [:div.slots-page
     [:h1 "Time Slots"]
     [:div.slots-content
      [:div.slots-list
       [:h2 "Available Slots"]
       (if (empty? slots)
         [:p "No slots found"]
         [:table.slots-table
          [:thead
           [:tr
            [:th "Start Time"]
            [:th "End Time"]
            [:th "Available"]
            [:th "Actions"]]]
          [:tbody
           (for [slot slots]
             ^{:key (:slot/id slot)}
             [:tr
              [:td (str (:slot/start-time slot))]
              [:td (str (:slot/end-time slot))]
              [:td (if (:slot/available slot) "Yes" "No")]
              [:td
               [:button "Edit"]
               [:button "Delete"]]])]])]
      
      [:div.add-slot-form
       [:h2 "Create New Slot"]
       [:p "Slot form coming soon..."]]]]))
