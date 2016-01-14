(ns weather-page.logic
  (:require [weather-page.sunrise :refer [rising-time setting-time]]
            [weather-page.condition-icons :refer [condition-icons]]
            [cljs-time.core :as t]))

(defn sunrise-params [location date]
  (let [date-params {:month        (t/month date)
                     :day          (t/day date)
                     :year         (t/year date)
                     :local-offset (.getTimezoneOffset date)}]
    (merge location date-params)))

(defn condition-icon [{:keys [location time weather]}]
  (when weather
    (let [sunrise-params (sunrise-params location time)
          sunrise (rising-time sunrise-params)
          sunset (setting-time sunrise-params)
          current-time (+ (t/hour time) (/ (t/minutes time) 60))
          is-daytime (< sunrise current-time sunset)
          weather-norm (.replace weather #"^(Light|Heavy) " "")
          icon (first (condition-icons weather-norm))]
      (if (coll? icon)
        (if is-daytime
          (first icon)
          (last icon))
        icon))))

; 1st number is the upper limit in kph
; 2nd number is the number on the scale
(def beaufort-scale [[1    0]
                     [6    1]
                     [12   2]
                     [20   3]
                     [29   4]
                     [39   5]
                     [50   6]
                     [62   7]
                     [75   8]
                     [89   9]
                     [103  10]
                     [117  11]
                     [1000 12]])

(defn kph->beaufort
  "Returns the Beaufort number for a given windspeed"
  [wind-kph]
  (last (first (filter #(> (first %) wind-kph) beaufort-scale))))

