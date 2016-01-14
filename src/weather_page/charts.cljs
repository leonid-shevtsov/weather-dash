(ns weather-page.charts
  (:require cljsjs.highcharts
            [om.core :as om]
            [sablono.core :refer-macros [html]]
            [om-tools.core :refer-macros [defcomponent]]))

(defonce HighchartsChart (.-Chart js/Highcharts))

(defn render-highcharts [owner config]
  (let [node (om/get-node owner)]
    (om/set-state! owner :chart (HighchartsChart. (clj->js (assoc-in config [:chart :renderTo] node))))))

(defcomponent highchart [{:keys [width height config]} owner]
  (render [_]
    (html [:div {:style {:height height :width width}}]))
  (did-mount [_]
    (render-highcharts owner config))
  (will-receive-props [_ {:keys [config]}]
    (when-let [chart (om/get-state owner :chart)] (.destroy chart))
    (render-highcharts owner config)))
