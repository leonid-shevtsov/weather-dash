(ns weather-page.core
  (:require [om.core :as om :include-macros true]
            [weather-page.data-fetcher :as data-fetcher :refer [fetch-data-periodically]]
            [weather-page.components.page :refer [page]]
            cljsjs.highcharts)
  (:require-macros [weather-page.config :refer [config]]))

(enable-console-print!)

(defonce app-state
         (atom {:config {:api-key    (config :api-key)
                         :lang       (config :lang)
                         :station-id (config :station-id)
                         :units      :metric}}
               {:location   {:latitude  0
                             :longitude 0}
                :conditions {:time        nil
                             :weather     ""
                             :temperature 0
                             :feelslike   0
                             :wind        0
                             :wind_kph    0}
                :forecast   []}))

(defonce setup
         (let [state-cursor (om/root-cursor app-state)]
           (.setOptions js/Highcharts (clj->js {:global {:useUTC false} :lang {:shortMonths ["Янв" "Фев" "Мар" "Апр" "Май" "Июн" "Июл" "Авг" "Сен" "Окт" "Ноя" "Дек"]}}))
           (fetch-data-periodically {:state-cursor state-cursor
                                     :url-fn       data-fetcher/conditions-url
                                     :updater      data-fetcher/update-conditions
                                     :timeout      (* 60 5 1000)})
           (fetch-data-periodically {:state-cursor state-cursor
                                     :url-fn       data-fetcher/forecast-url
                                     :updater      data-fetcher/update-forecast
                                     :timeout      (* 60 30 1000)})))

(om/root page app-state {:target (.getElementById js/document "app")})
