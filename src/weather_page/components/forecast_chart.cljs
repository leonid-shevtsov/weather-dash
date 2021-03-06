(ns weather-page.components.forecast-chart
  (:require [om.core :as om]
            [cljs-time.core :as t]
            [cljs-time.coerce :as tc]
            [weather-page.logic :as logic]
            [weather-page.sunrise :refer [rising-time setting-time]]
            [weather-page.charts :refer [highchart]]))

(defonce setup-highcharts
         (.setOptions js/Highcharts (clj->js {:global {:useUTC false} :lang {:shortMonths (range 1 12)}})))

(defn average-color [color-1 color-2 fraction-1 fraction-2]
  (map #(Math/round (+ (* %1 fraction-1)
                       (* %2 fraction-2)))
       color-1
       color-2))

(defn color->rgba [color opacity]
  (str "rgba(" (nth color 0) "," (nth color 1) "," (nth color 2) "," opacity ")"))

(defn make-band [{:keys [time timeofday cloudcover]} {end-time :time}]
  (let [cloud-color (get {:day [200 200 200] :night [50 50 50]} timeofday)
        sky-color (get {:day [10 123 255] :night [6 50 100]} timeofday)
        cloud-fraction (/ cloudcover 100.0)
        sky-fraction (- 1.0 cloud-fraction)
        color (average-color cloud-color sky-color cloud-fraction sky-fraction)]
    {:color (color->rgba color 1.0) :from time :to end-time}))

(defn forecast-days [forecast]
  (map (comp t/to-default-time-zone tc/from-long)
       (distinct (map (comp tc/to-long t/at-midnight :time)
                      forecast))))

(def celcius-line-data [[-20 "#3b0cbd"] [-10 "#4444dd"] [0 "#12bdb9"] [10 "#69d025"] [20 "#d0c310"] [30 "#ff9933"] [40 "#ff3311"]])
(def fahrenheit-line-data [[0 "#3b0cbd"] [20 "#4444dd"] [40 "#12bdb9"] [60 "#69d025"] [80 "#d0c310"] [100 "#ff9933"] [120 "#ff3311"]])

(defn temp-lines [units page]
  (let [line-data (case units
                    "metric" celcius-line-data
                    "imperial" fahrenheit-line-data)]
    (map #(merge {:width (/ (:width page) 160) :zIndex 1} (zipmap [:value :color] %1)) line-data)))

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

(defn precipitation-shade [{:keys [rain-amount snow-amount]}]
  (let [light-color [0 115 255]
        intense-color [199 0 174]
        precipitation-treshold 10
        total-amount (* 1.0 (+ rain-amount snow-amount))]
    (if (>= total-amount precipitation-treshold)
      (color->rgba intense-color 0.5)
      (color->rgba light-color 0.5))))

(defn precipitation-zones [forecast]
  (let [color (map precipitation-shade forecast)
        time (map #(tc/to-long (:time %)) (rest forecast))]
    (map #(zipmap [:color :fillColor :value] %&) color color time)))

(defn forecast-chart [{page :page forecast :forecast location :location {units :units} :config} _owner]
  (reify
    om/IRender
    (render [_]
      (let [time-start (-> forecast first :time t/to-default-time-zone tc/to-long)
            time-interval (* 3 3600 1000)
            temp-line (map :temperature forecast)
            prec-line (map :precipitation forecast)
            prec-zones (precipitation-zones forecast)
            plot-bands (forecast-bands location forecast)
            plot-lines (forecast-lines forecast)
            temp-min (- (apply min temp-line) 1)
            temp-max (+ (apply max temp-line) 1)
            common-series-config {:animation           false
                                  :pointStart          time-start
                                  :pointInterval       time-interval
                                  :marker              {:enabled false}
                                  :enableMouseTracking false}
            label-style {:color :white :font-weight :bold}]
        (om/build highchart {:width  (:width page) :height (/ (:height page) 2)
                             :config {:chart   {:type            "line"
                                                :alignTicks      false
                                                :backgroundColor "black"
                                                :animation       false}
                                      :legend  {:enabled false}
                                      :tooltip {:enabled false}
                                      :credits {:enabled false}
                                      :title   {:text nil}
                                      :xAxis   {:type         "datetime"
                                                :plotBands    plot-bands
                                                :plotLines    plot-lines
                                                 :labels       {:x 60 :overflow "justify" :style label-style}
                                                :minPadding   0
                                                :maxPadding   0
                                                :tickInterval (* 86400 1000)
                                                :tickWidth (/ (:width page) 480)}
                                      :yAxis   [{:title         "Temperature"
                                                 :allowDecimals false
                                                 :labels        {:style  label-style
                                                                 :format "{value}°"}
                                                 :min           temp-min
                                                 :max           temp-max
                                                 :startOnTick   false
                                                 :endOnTick     false
                                                 :tickInterval  5
                                                 :plotLines     (temp-lines units page)}
                                                {:title         "Chance of rain"
                                                 :opposite      true
                                                 :visible       false
                                                 :max           100
                                                 :min           0
                                                 :tickAmount    5
                                                 :gridLineWidth 0
                                                 :labels        {:style  label-style
                                                                 :format "{value}%"}}]
                                      :series  (map #(merge common-series-config %)
                                                    [{:data     prec-line
                                                      :name     "Precipitation"
                                                      :yAxis    1
                                                      :type     "areaspline"
                                                      :zones    prec-zones
                                                      :zoneAxis "x"}
                                                     {:data      temp-line
                                                      :name      "Temperature"
                                                      :type      "spline"
                                                      :lineWidth (/ (:width page) 96)
                                                      :color     "rgb(195,5,0)"}
                                                     (comment {:data      feels-line
                                                               :name      "FeelsLike"
                                                               :type      "spline"
                                                               :lineWidth (/ (:width page) 240)
                                                               :color     "rgba(240,50,0, 0.5)"})])}})))))
