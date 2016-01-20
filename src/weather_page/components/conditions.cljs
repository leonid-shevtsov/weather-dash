(ns weather-page.components.conditions
  (:require [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [cljs-time.format :as tf]
            [weather-page.logic :refer [kph->beaufort]]
            [weather-page.condition-icons :refer [condition-icon]]))

(defn capitalize [string]
  (let [downcase (.toLocaleLowerCase string)]
    (apply str (.toLocaleUpperCase (first downcase)) (rest downcase))))

(defcomponent conditions [{:keys [feelslike temperature wind wind_kph weather icon_url]} _owner]
  (render [_]
    (html [:.forecast
           [:.forecast-block
            [:.huge temperature "°"]
            [:.details
             "Кажется: " [:b feelslike "°"]]]
           [:.forecast-block
            [:.huge
             [:i {:class (str "wi wi-" (condition-icon icon_url))}]]
            [:.details (capitalize weather)]]
           [:.forecast-block
            [:.huge
             [:i {:class (str "wi wi-wind-beaufort-" (kph->beaufort wind_kph))}]]
            [:.details "Ветер " wind " км/ч"]]])))
