(ns frontend.views.main
  (:require [re-frame.core :as rf]
            [frontend.views.home :as home]
            [frontend.views.login :as login]
            [frontend.views.admin :as admin]
            [frontend.views.patients :as patients]
            [frontend.views.appointments :as appointments]
            [frontend.views.slots :as slots]
            [frontend.views.dashboard :as dashboard]))

(defn scroll-to-section [section-id]
  (when-let [element (.getElementById js/document section-id)]
    (.scrollIntoView element #js {:behavior "smooth" :block "start"})))

(defn navigation []
  (let [current-page @(rf/subscribe [:current-page])
        authenticated? @(rf/subscribe [:authenticated?])
        user @(rf/subscribe [:user])]
    [:nav.navbar
     [:div.nav-brand
      [:h1 {:on-click #(rf/dispatch [:set-current-page :home])} 
       "PhysioLife Clinic"]]
     [:div.nav-links
      (if (= current-page :home)
        ;; One-page navigation when on home
        [:<>
         [:a {:on-click #(scroll-to-section "hero")} "Home"]
         [:a {:on-click #(scroll-to-section "services")} "Services"]
         [:a {:on-click #(scroll-to-section "about")} "About"]
         [:a {:on-click #(scroll-to-section "booking")} "Book Now"]
         [:a {:on-click #(scroll-to-section "contact")} "Contact"]]
        ;; Regular navigation for other pages
        [:<>
         [:a {:class (when (= current-page :home) "active")
              :on-click #(rf/dispatch [:set-current-page :home])}
          "Home"]
         [:a {:on-click (fn []
                        (rf/dispatch [:set-current-page :home])
                        (js/setTimeout (fn [] (scroll-to-section "services")) 100))}
          "Services"]
         [:a {:on-click (fn []
                        (rf/dispatch [:set-current-page :home])
                        (js/setTimeout (fn [] (scroll-to-section "about")) 100))}
          "About"]
         [:a {:on-click (fn []
                        (rf/dispatch [:set-current-page :home])
                        (js/setTimeout (fn [] (scroll-to-section "contact")) 100))}
          "Contact"]])
      
      ;; Authentication and admin links
      (if authenticated?
        [:div.admin-nav
         (if (= (:role user) "admin")
           [:a {:class (when (= current-page :admin) "active")
                :on-click #(rf/dispatch [:set-current-page :admin])}
            "Admin Dashboard"]
           [:a {:class (when (= current-page :dashboard) "active")
                :on-click #(rf/dispatch [:set-current-page :dashboard])}
            "Dashboard"])
         [:a {:on-click #(rf/dispatch [:logout])}
          "Logout"]]
        [:a {:class (when (= current-page :login) "active")
             :on-click #(rf/dispatch [:set-current-page :login])}
         "Staff Login"])]]))

(defn error-banner []
  (when-let [error @(rf/subscribe [:error])]
    [:div.error-banner
     [:p error]
     [:button {:on-click #(rf/dispatch [:clear-error])} "×"]]))

(defn loading-spinner []
  (when @(rf/subscribe [:loading])
    [:div.loading-overlay
     [:div.spinner]]))

(defn footer []
  [:footer.footer
   [:div.container
    [:div.footer-content
     [:div.footer-section
      [:h3 "PhysioLife Clinic"]
      [:p "Professional physiotherapy services for your active lifestyle. Expert care, personalized treatment, lasting results."]]
     
     [:div.footer-section
      [:h4 "Quick Links"]
      [:ul
       [:li [:a {:on-click #(scroll-to-section "services")} "Services"]]
       [:li [:a {:on-click #(scroll-to-section "about")} "About"]]
       [:li [:a {:on-click #(scroll-to-section "booking")} "Book Appointment"]]
       [:li [:a {:on-click #(scroll-to-section "contact")} "Contact"]]]]
     
     [:div.footer-section
      [:h4 "Contact Info"]
      [:p "123 Health Street, Medical District"]
      [:p "Phone: (555) 123-4567"]
      [:p "Email: info@physiolife.com"]]
     
     [:div.footer-section
      [:h4 "Hours"]
      [:p "Mon-Fri: 7:00 AM - 7:00 PM"]
      [:p "Saturday: 8:00 AM - 4:00 PM"]
      [:p "Sunday: Closed"]]]
    
    [:div.footer-bottom
     [:p "© 2025 PhysioLife Clinic. All rights reserved."]]]])

(defn main-panel []
  (let [current-page @(rf/subscribe [:current-page])
        authenticated? @(rf/subscribe [:authenticated?])
        user @(rf/subscribe [:user])]
    [:div.app
     [loading-spinner]
     [error-banner]
     [navigation]
     [:main.main-content
      (case current-page
        :home [home/home-page]
        :login [login/login-page]
        :admin (if (and authenticated? (= (:role user) "admin"))
                 [admin/admin-panel]
                 [login/login-page])
        :dashboard (if authenticated?
                    [dashboard/dashboard-page]
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
        [home/home-page])]
     (when (= current-page :home)
       [footer])]))
