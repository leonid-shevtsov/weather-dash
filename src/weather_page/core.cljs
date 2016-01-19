(ns weather-page.core
  (:require [om.core :as om :include-macros true]
            [weather-page.state :refer [app-state app-cursor]]
            [weather-page.data-fetcher :as data-fetcher :refer [fetch-data-periodically]]
            [weather-page.components.router :refer [router]]

            cljsjs.highcharts))

(enable-console-print!)

(defonce setup
         (do
           (.setOptions js/Highcharts (clj->js {:global {:useUTC false} :lang {:shortMonths ["Янв" "Фев" "Мар" "Апр" "Май" "Июн" "Июл" "Авг" "Сен" "Окт" "Ноя" "Дек"]}}))
           (fetch-data-periodically {:state-cursor app-cursor
                                     :url          (data-fetcher/conditions-url (:config app-cursor))
                                     :updater      data-fetcher/update-conditions
                                     :timeout      (* 60 5 1000)})
           (fetch-data-periodically {:state-cursor app-cursor
                                     :url          (data-fetcher/forecast-url (:config app-cursor))
                                     :updater      data-fetcher/update-forecast
                                     :timeout      (* 60 30 1000)})))

(om/root router app-state {:target (.getElementById js/document "app")})
