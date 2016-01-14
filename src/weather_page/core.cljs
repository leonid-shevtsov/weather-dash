(ns weather-page.core
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [weather-page.data-fetcher :as data-fetcher :refer [fetch-data-periodically]]
            [weather-page.charts :refer [highchart]]
            [weather-page.sunrise :refer [rising-time setting-time]]))

(enable-console-print!)

(defonce app-state (atom {:conditions {}
                          :forecast {}}))

(defonce setup
         (do (.setOptions js/Highcharts (clj->js {:global {:useUTC false} :lang {:shortMonths ["Янв" "Фев" "Мар" "Апр" "Май" "Июн" "Июл" "Авг" "Сен" "Окт" "Ноя" "Дек"]}}))
             (fetch-data-periodically {:cursor (om/ref-cursor (:conditions (om/root-cursor app-state)))
                                       :url data-fetcher/conditions-url
                                       :key :current_observation
                                       :timeout (* 60 5 1000)})
             (fetch-data-periodically {:cursor (om/ref-cursor (:forecast (om/root-cursor app-state)))
                                       :url data-fetcher/forecast-url
                                       :key :hourly_forecast
                                       :timeout (* 60 30 1000)})))

(defn forecast-label [{{:keys [hour_padded weekday_name]} :FCTTIME}]
  (if (= "00" hour_padded)
    weekday_name
    hour_padded))

(def condition-icons (reduce #(assoc %1 (first %2) (rest %2)) {} [
["Drizzle" ["day-sprinkle" "night-alt-sprinkle"] "Морось"]
["Rain" ["day-rain" "night-alt-rain"] "Дождь"]
["Snow" ["day-snow" "night-alt-snow"] "Снег"]
["Snow Grains" ["day-snow" "night-alt-snow"] "Снежные зерна"]
["Ice Crystals" ["day-snow" "night-alt-snow"] "Ледяные кристаллы"]
["Ice Pellets" ["day-snow" "night-alt-snow"] "Ледяная крупа"]
["Hail" ["day-hail" "night-alt-hail"] "Град"]
["Mist" ["day-fog" "night-fog"] "Туман"]
["Fog" ["day-fog" "night-fog"] "Туман"]
["Fog Patches" ["day-fog" "night-fog"] "Туман местами"]
["Smoke" "smoke" "Дым"]
["Volcanic Ash" "smoke" "Вулканический пепел"]
["Widespread Dust" "dust" "Пыль"]
["Sand" "sandstorm" "Песок"]
["Haze" "day-haze" "Дымка"]
["Spray" ["day-sprinkle" "night-alt-sprinkle"] "Брызги"]
["Dust Whirls" "dust" "Пыльные вихри"]
["Sandstorm" "sandstorm" "Песчаная буря"]
["Low Drifting Snow" ["day-snow" "night-alt-snow"] "Снежная поземка"]
["Low Drifting Widespread Dust" "dust" "Пыльная поземка"]
["Low Drifting Sand" "sandstorm" "Песчаная поземка"]
["Blowing Snow" ["day-snow-wind" "night-alt-snow-wind"] "Метель"]
["Blowing Widespread Dust" "dust" "Пыльная буря"]
["Blowing Sand" "sandstorm" "Песчаная буря"]
["Rain Mist" ["day-sprinkle" "night-alt-sprinkle"] "Дождевая мгла"]
["Rain Showers" ["day-rain" "night-alt-rain"] "Ливень"]
["Snow Showers" ["day-snow" "night-alt-snow"] "Ливневый снег"]
["Snow Blowing Snow Mist" ["day-snow" "night-alt-snow"] "Снежная пыль"]
["Ice Pellet Showers" ["day-snow" "night-alt-snow"] "Ледяная крупа"]
["Hail Showers" ["day-hail" "night-alt-hail"] "Град"]
["Small Hail Showers" ["day-hail" "night-alt-hail"] "Мелкий град"]
["Thunderstorm" ["day-thunderstorm" "night-alt-thunderstorm"] "Гроза"]
["Thunderstorms and Rain" ["day-thunderstorm" "night-alt-thunderstorm"] "Гроза с дождем"]
["Thunderstorms and Snow" ["day-snow-thunderstorm" "night-alt-snow-thunderstorm"] "Гроза со снегом"]
["Thunderstorms and Ice Pellets" ["day-snow-thunderstorm" "night-alt-snow-thunderstorm"] "Гроза с ледяной крупой"]
["Thunderstorms with Hail" ["day-sleet-storm" "night-alt-sleet-storm"] "Гроза с градом"]
["Thunderstorms with Small Hail" ["day-sleet-storm" "night-alt-sleet-storm"] "Гроза с мелким градом"]
["Freezing Drizzle" ["day-rain-mix" "night-alt-rain-mix"] "Изморозь"]
["Freezing Rain" ["day-rain-mix" "night-alt-rain-mix"] "Ледяной дождь"]
["Freezing Fog" ["day-fog" "night-fog"] "Замерзающий туман"]
["Patches of Fog" ["day-fog" "night-fog"] "Местами туман"]
["Shallow Fog" ["day-fog" "night-fog"] "Неглубокий туман"]
["Partial Fog" ["day-fog" "night-fog"] "Частичный туман"]
["Overcast" "cloudy" "Пасмурно"]
["Clear" ["day-sunny" "night-clear"] "Ясно"]
["Partly Cloudy" ["day-cloudy" "night-alt-cloudy"] "Переменная облачность"]
["Mostly Cloudy" ["day-cloudy" "night-alt-cloudy"] "Облачно"]
["Scattered Clouds" ["day-cloudy" "night-alt-cloudy"] "Рассеянные облака"]
["Small Hail" ["day-hail" "night-alt-hail"] "Мелкий град"]
["Squalls" ["day-windy" "windy"] "Шквалы"]
["Funnel Cloud" "tornado" "Трубообразное облако"]
["Unknown Precipitation" "alien" "Неизвестные осадки"]
["Unknown" "alien" "Неизвестно"]
]))

(defn sunrise-params [conditions]
  {:latitude     (js/parseFloat (get-in conditions [:observation_location :latitude]))
   :longitude    (js/parseFloat (get-in conditions [:observation_location :longitude]))
   ; FIXME timezone calculation will not work with fractional timezones
   :local-offset (/ (js/parseInt (get conditions :local_tz_offset)) 100)})

(defn condition-icon [conditions]
  (when (:weather conditions)
    (let [sunrise-params (sunrise-params conditions)
          date (js/Date. (:local_time_rfc822 conditions))
          sunrise-params (assoc sunrise-params :month (inc (.getMonth date)) :day (.getDate date) :year (.getYear date))
          sunrise (rising-time sunrise-params)
          sunset (setting-time sunrise-params)
          current-time (+ (.getHours date) (/ (.getMinutes date) 60))
          is-daytime (< sunrise current-time sunset)
          weather-norm (.replace (:weather conditions) #"^(Light|Heavy) " "")
          icons (first (condition-icons weather-norm))]
      (if (coll? icons) (if is-daytime (first icons) (last icons)) icons))))

(defn weather-label [weather]
  (when weather
    (let [weather-norm (.replace weather #"^(Light|Heavy) " "")
          label (last (condition-icons weather-norm))]
      (cond
        (.match weather #"^Light ") (str "Слаб. " (.toLocaleLowerCase label))
        (.match weather #"^Heavy ") (str "Сильн. " (.toLocaleLowerCase label))
        :else label))))

(defn kph->beaufort [wind-kph]
  (let [scale [[1    0]
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
               [1000 12]]]
    (last (first (filter #(> (first %) wind-kph) scale)))))

(defn make-band [{:keys [time timeofday sky]} {end-time :time}]
  (let [cloud-color (get {:day [200 200 200] :night [50 50 50]} timeofday)
        sky-color (get {:day [10 123 255] :night [6 50 100]} timeofday)
        cloud-fraction (/ sky 100.0)
        sky-fraction (- 1 cloud-fraction)
        color (map #(Math/round (+ (* %1 cloud-fraction) (* %2 sky-fraction))) cloud-color sky-color)]
    {:color (str "rgb(" (.join (clj->js color) ",") ")") :from time :to end-time}))

(defn chart-wrapper [{:keys [forecast conditions]} _owner]
  (reify
    om/IRender
  (render [_]
    (let [forecast (take 72 forecast)
          time-start (-> forecast first :FCTTIME :epoch js/parseInt (* 1000))
          time-interval (* 3600 1000)
          temp-line (map #(js/parseInt (get-in % [:temp :metric]) 10) forecast)
          feels-line (map #(js/parseInt (get-in % [:feelslike :metric]) 10) forecast)
          wind-line (map #(kph->beaufort (js/parseInt (get-in % [:wspd :metric]) 10)) forecast)
          prec-line (map #(js/parseInt (get % :pop) 10) forecast)
          sunrise-params (sunrise-params conditions)
                dates (map #(merge % sunrise-params)
                     (distinct (map #(zipmap [:day :month :year]
                                        [(js/parseInt (get-in % [:FCTTIME :mday]))
                                        (js/parseInt (get-in % [:FCTTIME :mon]))
                                        (js/parseInt (get-in % [:FCTTIME :year]))]) forecast)))

          ;
          ;suntimes (mapcat #(list (+ %2 (rising-time %1)) (+ %2 (setting-time %1))) dates (iterate (partial + 24) 0))
          ;nights (->> (concat '(0) suntimes (list (* 24 (count dates))))
          ;            (map #(+ time-start (* 60 60 1000 (- % (-> forecast first :FCTTIME :hour js/parseInt)))))
          ;            (partition 2))
          ;day-starts (map #(* 1000 (js/parseInt (get-in % [:FCTTIME :epoch]))) (filter #(= (get-in % [:FCTTIME :hour]) "0") forecast))
          ;

          suntimes (->> (mapcat #(list (+ %2 (rising-time %1)) (+ %2 (setting-time %1))) dates (iterate (partial + 24) 0))
                        (map #(+ time-start (* 60 60 1000 (- % (-> forecast first :FCTTIME :hour js/parseInt))))))
          sun-events (map #(zipmap [:time :timeofday] %&) suntimes (cycle [:day :night]))
          forecast-events (map #(array-map :time (* 1000 (js/parseInt (get-in % [:FCTTIME :epoch]))) :sky (get % :sky)) forecast)
          all-events (sort-by :time (concat sun-events forecast-events))
          state (reductions merge {:time 0 :timeofday :night :sky (-> all-events first :sky)} all-events)
          bands (map make-band state (rest state))
          day-starts (map #(* 1000 (js/parseInt (get-in % [:FCTTIME :epoch]))) (filter #(= (get-in % [:FCTTIME :hour]) "0") forecast))
          temp-min (- (apply min (concat temp-line feels-line)) 1)
          temp-max (+ (apply max (concat temp-line feels-line)) 1)]
      (om/build highchart {:width 480 :height 160
                           :config {:chart {:type "line" :alignTicks false :backgroundColor "black" :animation false}
                                    :legend {:enabled false}
                                    :tooltip {:enabled false}
                                    :title {:text nil}
                                    :xAxis {:type "datetime"
                                            :plotBands bands
                                            :plotLines (map #(array-map :value % :color "#777" :width 2 :zIndex 3) day-starts)
                                            :labels {:x 60 :overflow "justify" :style {:color :white :font-weight :bold}}
                                            :minPadding 0
                                            :maxPadding 0}
                                    :yAxis [{:title "Temperature" :allowDecimals false
                                             :labels {:style {:color :white :font-weight :bold} :format "{value}°"}
                                             :min temp-min :max temp-max :startOnTick false :endOnTick false :tickInterval 5}
                                            {:title "Chance of rain" :opposite true :max 100 :min 0 :tickAmount 5 :gridLineWidth 0 :labels {:style {:color :white :font-weight :bold} :format "{value}%"}}]
                                            ;{:title "Wind" :opposite true :max 12 :min 0 :allowDecimals false :gridLineWidth 0}]
                                    :series [{:name "Temp" :animation false :type "spline" :lineWidth 5 :data temp-line :color "rgb(195,5,0)" :pointStart time-start :pointInterval time-interval :marker {:enabled false} :enableMouseTracking false}
                                             {:name "FeelsLike" :animation false :type "spline" :data feels-line :color "rgba(240,50,0, 0.5)" :pointStart time-start :pointInterval time-interval :marker {:enabled false} :enableMouseTracking false}
                                             ;{:type "column" :name "wind" :data wind-line :color "#001f3f" :pointStart time-start :pointInterval time-interval :yAxis 2 :marker {:enabled false}}
                                             {:name "Precipitation" :animation false :data prec-line :yAxis 1 :type "area" :color "rgba(33,83,149,0.2)" :pointStart time-start :pointInterval time-interval  :marker {:enabled false} :enableMouseTracking false}]}})
    ))))

(defcomponent
  page [{:as data {:keys [feelslike_c temp_c wind_kph weather observation_time]} :conditions} _owner]
  (render [_]
    (html [:div
                 [:.forecast
                  [:.updated-at observation_time]
                 [:.forecast-block
                  [:.huge temp_c "°"]
                  [:.details
                    "Кажется: " [:b feelslike_c "°"]
                   ]
                  ]
                 [:.forecast-block
                  [:.huge
                   [:i {:class (str "wi wi-" (condition-icon (:conditions data)))}]]
                  [:.details (weather-label weather)]
                  ]
                 [:.forecast-block
                  [:.huge
                   [:i {:class (str "wi wi-wind-beaufort-" (kph->beaufort wind_kph))}]]
                  [:.details "Ветер " wind_kph " км/ч"]]]
           (om/build chart-wrapper data)
           ])))


(om/root page app-state {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
