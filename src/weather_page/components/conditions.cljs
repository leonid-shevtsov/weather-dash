(ns weather-page.components.conditions
  (:require [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [cljs-time.format :as tf]
            [weather-page.logic :refer [kph->beaufort]]
            [weather-page.condition-icons :refer [condition-icons]]))

(defn capitalize [string]
  (let [downcase (.toLocaleLowerCase string)]
    (apply str (.toLocaleUpperCase (first downcase)) (rest downcase))))

(defcomponent conditions [{:keys [feelslike temperature wind wind_kph weather icon time]} _owner]
  (render [_]
    (html [:.forecast
           [:.updated-at (tf/unparse (tf/formatter "HH:mm") time)]
           [:.forecast-block
            [:.huge temperature "°"]
            [:.details
             "Кажется: " [:b feelslike "°"]]]
           [:.forecast-block
            [:.huge
             [:i {:class (str "wi wi-" (get condition-icons icon "alien"))}]]
            [:.details (capitalize weather)]]
           [:.forecast-block
            [:.huge
             [:i {:class (str "wi wi-wind-beaufort-" (kph->beaufort wind_kph))}]]
            [:.details "Ветер " wind " км/ч"]]])))
