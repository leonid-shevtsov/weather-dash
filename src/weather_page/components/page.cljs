(ns weather-page.components.page
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [cljs-time.format :as tf]
            [weather-page.logic :refer [kph->beaufort]]
            [weather-page.condition-icons :refer [condition-icons]]
            [weather-page.components.forecast-chart :refer [forecast-chart]]))

(defcomponent
  page [{:as data {:keys [feelslike temperature wind wind_kph weather icon time]} :conditions} _owner]
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
                    [:i {:class (str "wi wi-" (get condition-icons icon "alien"))}]]
                   [:.details weather]
                   ]
                  [:.forecast-block
                   [:.huge
                    [:i {:class (str "wi wi-wind-beaufort-" (kph->beaufort wind_kph))}]]
                   [:.details "Ветер " wind " км/ч"]]]
                 (om/build forecast-chart data)
                 ])
          (html [:div "Loading..."]))))
