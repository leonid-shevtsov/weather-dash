(ns weather-page.components.page
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [cljs-time.format :as tf]
            [weather-page.logic :refer [condition-icon kph->beaufort]]
            [weather-page.components.forecast-chart :refer [forecast-chart]]))

(defcomponent
  page [{:as data {location :location :keys [feelslike temperature wind wind_kph weather time]} :conditions} _owner]
  (render [_]
    (if time
          (html [:div
                 [:.forecast
                  [:.updated-at (tf/unparse (tf/formatter "HH:mm") time)]
                  [:.forecast-block
                   [:.huge temperature "°"]
                   [:.details
                    "Кажется: " [:b feelslike "°"]
                    ]
                   ]
                  [:.forecast-block
                   [:.huge
                    [:i {:class (str "wi wi-" (condition-icon {:location location
                                                               :time time
                                                               :weather weather}))}]]
                   [:.details weather]
                   ]
                  [:.forecast-block
                   [:.huge
                    [:i {:class (str "wi wi-wind-beaufort-" (kph->beaufort wind_kph))}]]
                   [:.details "Ветер " wind " км/ч"]]]
                 (om/build forecast-chart data)
                 ])
          (html [:div "Loading..."]))))
