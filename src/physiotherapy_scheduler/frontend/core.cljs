(ns physiotherapy-scheduler.frontend.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [physiotherapy-scheduler.frontend.events]
            [physiotherapy-scheduler.frontend.subs]
            [physiotherapy-scheduler.frontend.views.main :as main]))

(defn ^:export init []
  (rf/dispatch-sync [:initialize-db])
  (dom/render [main/main-panel]
              (.getElementById js/document "app")))

(defn ^:export ^:dev/after-load reload []
  (init))
