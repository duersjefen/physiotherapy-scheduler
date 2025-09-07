(ns frontend.views.home)

(defn hero-section []
  [:section.hero
   [:div.hero-content
    [:h1 "Professional Physiotherapy Services"]
    [:p "Expert care for your recovery and wellbeing"]
    [:div.hero-buttons
     [:button.btn-primary {:on-click #(js/alert "Booking system coming soon!")}
      "Book Appointment"]
     [:button.btn-secondary {:on-click #(.scrollIntoView (.getElementById js/document "services"))}
      "Our Services"]]]])

(defn services-section []
  [:section#services.services
   [:h2 "Our Services"]
   [:div.services-grid
    [:div.service-card
     [:h3 "Sports Rehabilitation"]
     [:p "Specialized treatment for sports injuries and performance enhancement."]]
    [:div.service-card
     [:h3 "Manual Therapy"]
     [:p "Hands-on treatment to improve mobility and reduce pain."]]
    [:div.service-card
     [:h3 "Exercise Therapy"]
     [:p "Customized exercise programs for strength and flexibility."]]
    [:div.service-card
     [:h3 "Post-Surgery Recovery"]
     [:p "Comprehensive rehabilitation following surgical procedures."]]]])

(defn home-page []
  [:div.home-page
   [hero-section]
   [services-section]])

(defn services-page []
  [:div.services-page
   [:h1 "Our Services"]
   [:div.service-details
    [:div.service-item
     [:h2 "Sports Rehabilitation"]
     [:p "Our sports rehabilitation program is designed to help athletes recover from injuries and improve their performance. We use evidence-based treatments and work closely with coaches and trainers."]
     [:ul
      [:li "Injury assessment and diagnosis"]
      [:li "Sport-specific rehabilitation programs"]
      [:li "Performance optimization"]
      [:li "Injury prevention strategies"]]]
    
    [:div.service-item
     [:h2 "Manual Therapy"]
     [:p "Manual therapy involves hands-on techniques to diagnose and treat soft tissues and joint structures."]
     [:ul
      [:li "Joint mobilization"]
      [:li "Soft tissue massage"]
      [:li "Trigger point therapy"]
      [:li "Myofascial release"]]]
    
    [:div.service-item
     [:h2 "Exercise Therapy"]
     [:p "Personalized exercise programs designed to restore function and prevent future injuries."]
     [:ul
      [:li "Strength training"]
      [:li "Flexibility and mobility work"]
      [:li "Balance and coordination"]
      [:li "Home exercise programs"]]]]])

(defn about-page []
  [:div.about-page
   [:h1 "About Our Clinic"]
   [:div.about-content
    [:p "We are a team of qualified physiotherapists dedicated to helping our patients achieve their health and wellness goals. With years of experience and ongoing professional development, we provide the highest quality care."]
    
    [:h2 "Our Team"]
    [:div.team-grid
     [:div.team-member
      [:h3 "Dr. Sarah Johnson"]
      [:p "Lead Physiotherapist"]
      [:p "15+ years experience, specialized in sports medicine"]]
     
     [:div.team-member
      [:h3 "Dr. Michael Chen"]
      [:p "Physiotherapist"]
      [:p "10+ years experience, specialized in manual therapy"]]
     
     [:div.team-member
      [:h3 "Dr. Emma Davis"]
      [:p "Physiotherapist"]
      [:p "8+ years experience, specialized in post-surgical rehabilitation"]]]
    
    [:h2 "Our Facility"]
    [:p "Our modern clinic is equipped with state-of-the-art equipment and designed to provide a comfortable and professional environment for your treatment."]]])

(defn contact-page []
  [:div.contact-page
   [:h1 "Contact Us"]
   [:div.contact-content
    [:div.contact-info
     [:h2 "Get in Touch"]
     [:div.contact-item
      [:h3 "Address"]
      [:p "123 Health Street" [:br] "Medical District" [:br] "City, State 12345"]]
     
     [:div.contact-item
      [:h3 "Phone"]
      [:p "(555) 123-4567"]]
     
     [:div.contact-item
      [:h3 "Email"]
      [:p "info@physioclinic.com"]]
     
     [:div.contact-item
      [:h3 "Hours"]
      [:p "Monday - Friday: 8:00 AM - 6:00 PM" [:br]
         "Saturday: 9:00 AM - 2:00 PM" [:br]
         "Sunday: Closed"]]]
    
    [:div.contact-form
     [:h2 "Send us a Message"]
     [:form {:on-submit #(do (.preventDefault %) (js/alert "Thank you for your message! We'll get back to you soon."))}
      [:div.form-group
       [:label "Name"]
       [:input {:type "text" :required true}]]
      [:div.form-group
       [:label "Email"]
       [:input {:type "email" :required true}]]
      [:div.form-group
       [:label "Phone"]
       [:input {:type "tel"}]]
      [:div.form-group
       [:label "Message"]
       [:textarea {:rows 5 :required true}]]
      [:button.btn-primary {:type "submit"} "Send Message"]]]]])
