(ns frontend.db)

(def default-db
  {:current-page :home
   :admin-page :dashboard
   :loading false
   :error nil
   :user nil
   :patients []
   :appointments []
   :slots []
   :forms {:login {:username "" :password ""}
           :patient {:name "" :email "" :phone "" :date-of-birth ""}
           :appointment {:patient-id "" :slot-id "" :notes ""}
           :slot {:start-time "" :end-time ""}}})
