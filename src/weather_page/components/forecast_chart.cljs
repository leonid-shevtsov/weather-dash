(ns weather-page.components.forecast-chart
  (:require [om.core :as om]
            [cljs-time.core :as t]
            [cljs-time.coerce :as tc]
            [weather-page.logic :as logic]
            [weather-page.sunrise :refer [rising-time setting-time]]
            [weather-page.charts :refer [highchart]]))

(defn make-band [{:keys [time timeofday cloudcover]} {end-time :time}]
  (let [cloud-color (get {:day [200 200 200] :night [50 50 50]} timeofday)
        sky-color (get {:day [10 123 255] :night [6 50 100]} timeofday)
        cloud-fraction (/ cloudcover 100.0)
        sky-fraction (- 1.0 cloud-fraction)
        color (map #(Math/round (+ (* %1 cloud-fraction)
                                   (* %2 sky-fraction)))
                   cloud-color
                   sky-color)]
    {:color (str "rgb(" (.join (clj->js color) ",") ")") :from time :to end-time}))

(defn forecast-days [forecast]
  (distinct (map #(t/at-midnight (:time %)) forecast)))

(defn forecast-bands [location forecast]
  (let [days (forecast-days forecast)
        sunrise-params-for-days (map #(logic/sunrise-params location %) days)
        sun-hours (map (juxt rising-time setting-time) sunrise-params-for-days)
        sun-times (mapcat (fn [hours day] (map #(t/plus day (t/seconds (* 3600 %))) hours)) sun-hours days)
        sun-state-changes (map #(zipmap [:time :timeofday] %&) sun-times (cycle [:day :night]))
        cloud-state-changes (map #(select-keys % [:time :cloudcover]) forecast)
        all-state-changes (->> (concat sun-state-changes cloud-state-changes)
                               (map #(assoc % :time (tc/to-long (:time %))))
                               (sort-by :time))
        initial-state {:time 0
                       :timeofday :night
                       :cloudcover (-> forecast first :cloudcover)}
        state (reductions merge initial-state all-state-changes)]
    (map make-band state (rest state))))

(defn forecast-lines [forecast]
  (let [days (forecast-days forecast)]
    (map (fn [time] {:value time :color "#777" :width 2 :zIndex 3}) days)))

(defn forecast-chart [{forecast :forecast location :location {time :time} :conditions} _owner]
  (reify
    om/IRender
    (render [_]
      (let [time-start (tc/to-long time)
            time-interval (* 3600 1000)
            temp-line (map :temperature forecast)
            feels-line (map :feelslike forecast)
            prec-line (map :precipitation forecast)
            plot-bands (forecast-bands location forecast)
            plot-lines (forecast-lines forecast)
            temp-min (- (apply min (concat temp-line feels-line)) 1)
            temp-max (+ (apply max (concat temp-line feels-line)) 1)
            common-series-config {:animation false
                           :pointStart time-start
                           :pointInterval time-interval
                           :marker {:enabled false}
                           :enableMouseTracking false}
            label-style {:color :white :font-weight :bold}]
        (om/build highchart {:width  480 :height 160
                             :config {:chart {:type "line"
                                              :alignTicks false
                                              :backgroundColor "black"
                                              :animation false}
                                      :legend {:enabled false}
                                      :tooltip {:enabled false}
                                      :title {:text nil}
                                      :xAxis {:type "datetime"
                                              :plotBands plot-bands
                                              :plotLines plot-lines
                                              :labels {:x 60 :overflow "justify" :style label-style}
                                              :minPadding 0
                                              :maxPadding 0}
                                      :yAxis [{:title "Temperature"
                                               :allowDecimals false
                                               :labels {:style label-style
                                                        :format "{value}°"}
                                               :min temp-min
                                               :max temp-max
                                               :startOnTick false
                                               :endOnTick false
                                               :tickInterval 5}
                                              {:title "Chance of rain"
                                               :opposite true
                                               :max 100
                                               :min 0
                                               :tickAmount 5
                                               :gridLineWidth 0
                                               :labels {:style label-style
                                                        :format "{value}%"}}]
                                      :series (map #(merge common-series-config %)
                                                   [{:data prec-line
                                                     :name "Precipitation"
                                                     :yAxis 1
                                                     :type "area"
                                                     :color "rgba(33,83,149,0.2)"}
                                                    {:data temp-line
                                                     :name "Temperature"
                                                     :type "spline"
                                                     :lineWidth 5
                                                     :color "rgb(195,5,0)"}
                                                    {:data feels-line
                                                     :name "FeelsLike"
                                                     :type "spline"
                                                     :color "rgba(240,50,0, 0.5)"}])}})))))
