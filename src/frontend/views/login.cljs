(ns frontend.views.login
  (:require [re-frame.core :as rf]))

(defn login-form []
  (let [form @(rf/subscribe [:form :login])]
    [:div.login-container
     [:div.login-form
      [:h2 "Login"]
      [:form {:on-submit (fn [e]
                          (.preventDefault e)
                          (rf/dispatch [:login form]))}
       [:div.form-group
        [:label "Username"]
        [:input {:type "text"
                 :value (:username form)
                 :on-change #(rf/dispatch [:update-form :login :username (-> % .-target .-value)])}]]
       [:div.form-group
        [:label "Password"]
        [:input {:type "password"
                 :value (:password form)
                 :on-change #(rf/dispatch [:update-form :login :password (-> % .-target .-value)])}]]
       [:button {:type "submit"} "Login"]]
      [:div.demo-credentials
       [:h4 "Demo Credentials:"]
       [:p "Username: admin, Password: admin123"]
       [:p "Username: therapist, Password: therapist123"]]]]))

(defn login-page []
  [login-form])
