(ns frontend.views.main
  (:require [re-frame.core :as rf]
            [frontend.views.home :as home]
            [frontend.views.login :as login]
            [frontend.views.admin :as admin]
            [frontend.views.patients :as patients]
            [frontend.views.appointments :as appointments]
            [frontend.views.slots :as slots]))

(defn navigation []
  [:nav.navbar
   [:div.nav-brand
    [:h1 {:on-click #(rf/dispatch [:set-current-page :home])} 
     "Physiotherapy Clinic"]]
   [:div.nav-links
    [:a {:class (when (= @(rf/subscribe [:current-page]) :home) "active")
         :on-click #(rf/dispatch [:set-current-page :home])}
     "Home"]
    [:a {:class (when (= @(rf/subscribe [:current-page]) :services) "active")
         :on-click #(rf/dispatch [:set-current-page :services])}
     "Services"]
    [:a {:class (when (= @(rf/subscribe [:current-page]) :about) "active")
         :on-click #(rf/dispatch [:set-current-page :about])}
     "About"]
    [:a {:class (when (= @(rf/subscribe [:current-page]) :contact) "active")
         :on-click #(rf/dispatch [:set-current-page :contact])}
     "Contact"]
    (if @(rf/subscribe [:authenticated?])
      [:div.admin-nav
       [:a {:class (when (= @(rf/subscribe [:current-page]) :admin) "active")
            :on-click #(rf/dispatch [:set-current-page :admin])}
        "Admin"]
       [:a {:on-click #(rf/dispatch [:logout])}
        "Logout"]]
      [:a {:class (when (= @(rf/subscribe [:current-page]) :login) "active")
           :on-click #(rf/dispatch [:set-current-page :login])}
       "Staff Login"])]])

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
     [navigation]
     [:main.main-content
      (case current-page
        :home [home/home-page]
        :services [home/services-page]
        :about [home/about-page]
        :contact [home/contact-page]
        :login [login/login-page]
        :admin (if authenticated?
                 [admin/admin-panel]
                 [login/login-page])
        :patients (if authenticated?
                   [patients/patients-page]
                   [login/login-page])
        :appointments (if authenticated?
                       [appointments/appointments-page]
                       [login/login-page])
        :slots (if authenticated?
                [slots/slots-page]
                [login/login-page])
        [home/home-page])]]))
