(ns weather-page.components.page
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [weather-page.components.conditions :refer [conditions]]
            [weather-page.routing :refer [nav!]]
            [weather-page.components.forecast-chart :refer [forecast-chart]]))

(defcomponent page [{{time :time} :conditions :as data} _owner]
  (render [_]
    (if time
      (html [:div
             [:a {:onClick #(nav! "/settings")} "settings"]
             (om/build conditions (:conditions data))
             (om/build forecast-chart data)])
      (html [:div "Loading..."]))))
