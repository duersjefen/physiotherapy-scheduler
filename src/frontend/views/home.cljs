(ns frontend.views.home
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn hero-section []
  [:section#hero.hero
   [:div.container
    [:div.hero-content
     [:h1 "Serenity Massage Therapy"]
     [:p.subtitle "Professional therapeutic massage services for relaxation, recovery, and wellness"]
     [:div.hero-buttons
      [:a.btn-primary {:href "#booking"} "Book Massage"]
      [:a.btn-secondary {:href "#services"} "Our Massages"]]]]])

(defn services-section []
  [:section#services.services
   [:div.container
    [:h2.section-title "Our Professional Massage Services"]
    [:div.services-grid
     [:div.service-card
      [:div.service-icon "💆‍♀️"]
      [:h3 "Swedish Massage"]
      [:p "Classic relaxation massage using long, flowing strokes to relieve tension and promote overall well-being."]]
     
     [:div.service-card
      [:div.service-icon "�"]
      [:h3 "Deep Tissue Massage"]
      [:p "Targeted pressure therapy to release chronic muscle tension and address specific problem areas."]]
     
     [:div.service-card
      [:div.service-icon "�‍♂️"]
      [:h3 "Sports Massage"]
      [:p "Specialized techniques for athletes to enhance performance, prevent injuries, and speed recovery."]]
     
     [:div.service-card
      [:div.service-icon "🔥"]
      [:h3 "Hot Stone Massage"]
      [:p "Warm basalt stones combined with massage to melt away stress and deeply relax muscles."]]
     
     [:div.service-card
      [:div.service-icon "🤰"]
      [:h3 "Prenatal Massage"]
      [:p "Gentle, safe massage therapy specifically designed for expectant mothers to ease pregnancy discomfort."]]
     
     [:div.service-card
      [:div.service-icon "�"]
      [:h3 "Aromatherapy Massage"]
      [:p "Therapeutic massage enhanced with essential oils to promote relaxation and holistic healing."]]]]])

(defn about-section []
  [:section#about.about
   [:div.container
    [:div.about-content
     [:div.about-text
      [:h2 "Why Choose Serenity Massage Therapy?"]
      [:p "At Serenity, we believe in the healing power of therapeutic touch. Our certified massage therapists combine traditional techniques with modern wellness practices to provide you with a truly transformative experience."]
      [:p "Whether you need relief from stress, muscle tension, or simply want to indulge in some well-deserved relaxation, we create a personalized massage experience tailored to your unique needs."]
      [:ul.feature-list
       [:li "Certified and experienced massage therapists"]
       [:li "Tranquil, spa-like environment"]
       [:li "Customized massage treatments"]
       [:li "Premium organic oils and products"]
       [:li "Flexible scheduling including evenings"]
       [:li "Membership packages available"]]]
     
     [:div.about-image
      [:div.placeholder-image
       [:div.image-text "Professional Massage Therapy"]]]]]])

(defn booking-section []
  [:section#booking.booking
   [:div.container
    [:h2.section-title "Book Your Massage Session"]
    [:p.booking-subtitle "Choose from our available time slots and book your perfect massage experience. We'll send you a confirmation email with calendar details."]
    
    [:div.booking-container
     [:div.booking-steps
      [:div.step {:class (when @(rf/subscribe [:booking-step-active? 1]) "active")}
       [:div.step-number "1"]
       [:div.step-text "Select Service"]]
      [:div.step {:class (when @(rf/subscribe [:booking-step-active? 2]) "active")}
       [:div.step-number "2"] 
       [:div.step-text "Choose Time"]]
      [:div.step {:class (when @(rf/subscribe [:booking-step-active? 3]) "active")}
       [:div.step-number "3"]
       [:div.step-text "Your Details"]]]
     
     [:div.booking-form
      ;; Step 1: Service Selection
      (when @(rf/subscribe [:booking-step-active? 1])
        [:div.service-selection
         [:h3 "Choose Your Massage"]
         [:div.service-options
          [:div.service-option
           {:class (when @(rf/subscribe [:selected-service-is? "swedish"]) "selected")
            :on-click #(rf/dispatch [:select-service "swedish"])}
           [:div.service-info
            [:h4 "Swedish Massage"]
            [:p "60 minutes - $120"]
            [:p "Relaxation and stress relief"]]]
          
          [:div.service-option
           {:class (when @(rf/subscribe [:selected-service-is? "deep-tissue"]) "selected")
            :on-click #(rf/dispatch [:select-service "deep-tissue"])}
           [:div.service-info
            [:h4 "Deep Tissue Massage"]
            [:p "60 minutes - $140"]
            [:p "Targeted muscle tension relief"]]]
          
          [:div.service-option
           {:class (when @(rf/subscribe [:selected-service-is? "sports"]) "selected")
            :on-click #(rf/dispatch [:select-service "sports"])}
           [:div.service-info
            [:h4 "Sports Massage"]
            [:p "60 minutes - $150"]
            [:p "Athletic performance and recovery"]]]]
         
         [:button.btn-primary 
          {:disabled (not @(rf/subscribe [:has-selected-service?]))
           :on-click #(rf/dispatch [:next-booking-step])}
          "Continue to Time Selection"]])
      
      ;; Step 2: Time Selection (placeholder for now)
      (when @(rf/subscribe [:booking-step-active? 2])
        [:div.time-selection
         [:h3 "Select Your Preferred Time"]
         [:div.calendar-container
          [:p.placeholder-text "🗓️ Interactive calendar will be implemented here"]
          [:p "Available slots will show based on therapist availability"]]
         
         [:div.step-buttons
          [:button.btn-secondary {:on-click #(rf/dispatch [:prev-booking-step])} "Back"]
          [:button.btn-primary {:on-click #(rf/dispatch [:next-booking-step])} "Continue to Details"]]])
      
      ;; Step 3: Customer Details
      (when @(rf/subscribe [:booking-step-active? 3])
        [:form.customer-details
         {:on-submit (fn [e]
                       (.preventDefault e)
                       (rf/dispatch [:submit-massage-booking]))}
         [:h3 "Your Contact Information"]
         
         [:div.form-row
          [:div.form-group
           [:label {:for "name"} "Full Name *"]
           [:input {:type "text"
                    :id "name"
                    :required true
                    :placeholder "Enter your full name"
                    :value @(rf/subscribe [:form-value :booking :name])
                    :on-change #(rf/dispatch [:update-form :booking :name (-> % .-target .-value)])}]]
          
          [:div.form-group
           [:label {:for "email"} "Email Address *"]
           [:input {:type "email"
                    :id "email"
                    :required true
                    :placeholder "your.email@example.com"
                    :value @(rf/subscribe [:form-value :booking :email])
                    :on-change #(rf/dispatch [:update-form :booking :email (-> % .-target .-value)])}]]]
         
         [:div.form-row
          [:div.form-group
           [:label {:for "phone"} "Phone Number *"]
           [:input {:type "tel"
                    :id "phone"
                    :required true
                    :placeholder "+1 (555) 123-4567"
                    :value @(rf/subscribe [:form-value :booking :phone])
                    :on-change #(rf/dispatch [:update-form :booking :phone (-> % .-target .-value)])}]]]
         
         [:div.form-group
          [:label {:for "notes"} "Special Requests or Health Notes"]
          [:textarea {:id "notes"
                      :rows "3"
                      :placeholder "Any allergies, areas of focus, or special requests..."
                      :value @(rf/subscribe [:form-value :booking :notes])
                      :on-change #(rf/dispatch [:update-form :booking :notes (-> % .-target .-value)])}]]
         
         [:div.step-buttons
          [:button.btn-secondary {:type "button" :on-click #(rf/dispatch [:prev-booking-step])} "Back"]
          [:button.btn-primary {:type "submit"} "Book My Massage"]]])]]]])

(defn contact-section []
  [:section#contact.contact
   [:div.container
    [:h2.section-title "Visit Our Tranquil Studio"]
    [:div.contact-content
     [:div.contact-info
      [:h3 "Find Us"]
      [:div.contact-item
       [:div.contact-icon "📍"]
       [:div
        [:h4 "Studio Location"]
        [:p "456 Serenity Lane" [:br] "Wellness Center" [:br] "Your City, ST 12345"]]]
      
      [:div.contact-item
       [:div.contact-icon "📞"]
       [:div
        [:h4 "Reservations"]
        [:p [:a {:href "tel:+15551234567"} "+1 (555) 123-4567"]]]]
      
      [:div.contact-item
       [:div.contact-icon "✉️"]
       [:div
        [:h4 "Email"]
        [:p [:a {:href "mailto:info@serenitymassage.com"} "info@serenitymassage.com"]]]]
      
      [:div.contact-item
       [:div.contact-icon "🕒"]
       [:div
        [:h4 "Studio Hours"]
        [:p "Monday - Friday: 9:00 AM - 8:00 PM" [:br]
         "Saturday: 10:00 AM - 6:00 PM" [:br]
         "Sunday: 11:00 AM - 5:00 PM"]]]]
     
     [:div.contact-map
      [:div.map-placeholder
       [:div.map-text "Peaceful Location" [:br] "456 Serenity Lane"]]]]]])

(defn scroll-to [element-id]
  (when-let [element (.getElementById js/document element-id)]
    (.scrollIntoView element #js {:behavior "smooth" :block "start"})))

(defn home-page []
  [:div.home-page
   [hero-section]
   [services-section]
   [about-section]
   [booking-section]
   [contact-section]])