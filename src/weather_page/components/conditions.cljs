(ns weather-page.components.conditions
  (:require [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [cljs-time.format :as tf]
            [weather-page.logic :refer [kph->beaufort]]
            [weather-page.condition-icons :refer [condition-icon]]
            [weather-page.state :refer [app-state]]
            [weather-page.i18n :refer [t]]
            [om.core :as om]))

(defn capitalize [string]
  (let [downcase (.toLocaleLowerCase string)]
    (apply str (.toLocaleUpperCase (first downcase)) (rest downcase))))

(defn wind-dir-icon [wind-degrees]
  [:i {:class (str "wi wi-wind from-" wind-degrees "-deg")}])


(defcomponent conditions [{{:keys [feelslike temperature wind wind_kph wind_degrees weather icon_url]} :conditions page :page} owner]
  (render [_]
    (let [block-style {:style {:margin-top (/ (:height page) 15) :width (/ (:width page) 3) :vertical-align :middle}}
          huge-text-style {:style {:font-size (/ (:width page) 6)}}
          small-text-style {:style {:font-size (/ (:width page) 30)}}]
      (html [:.forecast {:style {:width "100%" :height (/ (:height page) 2)}}
             [:.forecast-block block-style
              [:.huge huge-text-style temperature "°"]
              [:.details small-text-style
               (t :conditions/feels-like) ": " [:b feelslike "°"]]]
             [:.forecast-block block-style
              [:.huge huge-text-style
               [:i {:class (str "wi wi-" (condition-icon icon_url))}]]
              [:.details small-text-style (capitalize weather)]]
             [:.forecast-block block-style
              [:.huge huge-text-style
               [:i {:class (str "wi wi-wind-beaufort-" (kph->beaufort wind_kph))}]]
              [:.details small-text-style
               (t :conditions/wind)
               " " (wind-dir-icon wind_degrees) " " wind " "
               (t (keyword (str "conditions.wind-units/" (:units @(om/get-shared owner :config)))))]]]))))
