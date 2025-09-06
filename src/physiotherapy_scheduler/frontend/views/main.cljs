(ns physiotherapy-scheduler.frontend.views.main
  (:require [re-frame.core :as rf]
            [physiotherapy-scheduler.frontend.views.login :as login]
            [physiotherapy-scheduler.frontend.views.dashboard :as dashboard]
            [physiotherapy-scheduler.frontend.views.patients :as patients]
            [physiotherapy-scheduler.frontend.views.appointments :as appointments]
            [physiotherapy-scheduler.frontend.views.slots :as slots]))

(defn navigation []
  (let [current-page @(rf/subscribe [:current-page])
        user @(rf/subscribe [:user])]
    (when user
      [:nav.navbar
       [:div.nav-brand
        [:h1 "Physiotherapy Scheduler"]]
       [:div.nav-links
        [:a {:class (when (= current-page :dashboard) "active")
             :on-click #(rf/dispatch [:set-current-page :dashboard])}
         "Dashboard"]
        [:a {:class (when (= current-page :patients) "active")
             :on-click #(rf/dispatch [:set-current-page :patients])}
         "Patients"]
        [:a {:class (when (= current-page :appointments) "active")
             :on-click #(rf/dispatch [:set-current-page :appointments])}
         "Appointments"]
        [:a {:class (when (= current-page :slots) "active")
             :on-click #(rf/dispatch [:set-current-page :slots])}
         "Slots"]
        [:a {:on-click #(rf/dispatch [:logout])}
         "Logout"]]])))

(defn error-banner []
  (when-let [error @(rf/subscribe [:error])]
    [:div.error-banner
     [:p error]
     [:button {:on-click #(rf/dispatch [:clear-error])} "×"]]))

(defn loading-spinner []
  (when @(rf/subscribe [:loading])
    [:div.loading-overlay
     [:div.spinner "Loading..."]]))

(defn main-panel []
  (let [current-page @(rf/subscribe [:current-page])
        authenticated? @(rf/subscribe [:authenticated?])]
    [:div.app
     [loading-spinner]
     [error-banner]
     (if authenticated?
       [:div
        [navigation]
        [:main.main-content
         (case current-page
           :dashboard [dashboard/dashboard]
           :patients [patients/patients-page]
           :appointments [appointments/appointments-page]
           :slots [slots/slots-page]
           [dashboard/dashboard])]]
       [login/login-page])]))
