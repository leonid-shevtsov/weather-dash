(ns weather-page.data-fetcher
  (:require [ajax.core :refer [GET POST]]
            [om.core :as om]
            [cljs-time.coerce :as tc]
            [cljs-time.local :as tl]))

(defn float-key [& key-path]
  (fn [hash-map] (js/parseFloat (get-in hash-map key-path))))

(def location-keys
  {:latitude  (float-key :observation_location :latitude)
   :longitude (float-key :observation_location :longitude)})

(defn conditions-keys [units]
  {:time        #(tl/from-local-string (get % :observation_time_rfc822))
   :weather     #(get % :weather)
   :icon        #(get % :icon)
   :temperature (float-key (if (= units :metric) :temp_c :temp_f))
   :feelslike   (float-key (if (= units :metric) :feelslike_c :feelslike_f))
   :wind        (float-key (if (= units :metric) :wind_kph :wind_mph))
   :wind_kph    (float-key :wind_kph)})                     ; for Beaufort scale

(defn forecast-keys [units]
  {:time #(tc/to-local-date-time (tc/from-long (* 1000 (js/parseInt (get-in % [:FCTTIME :epoch]) 10))))
   :temperature (float-key :temp units)
   :feelslike (float-key :feelslike units)
   :wind (float-key :wspd units)
   :precipitation (float-key :pop)
   :cloudcover (float-key :sky)})

(defn extract-keys [data keys]
  (reduce #(assoc %1 (get %2 0) ((get %2 1) data)) {} keys))

(def forecast-limit 72) ; hours to display as forecast

(defn extract-conditions [units response]
  (let [data (:current_observation response)
        location (extract-keys data location-keys)
        conditions (extract-keys data (conditions-keys units))]
    {:location location
     :conditions conditions}))

(defn extract-forecast [units response]
  (let [forecast-data (take forecast-limit (:hourly_forecast response))]
    (map #(extract-keys % (forecast-keys units)) forecast-data)))

(defn update-conditions [state-cursor response]
  (let [units (get-in state-cursor [:config :units])
        conditions (extract-conditions units response)]
    (om/transact! state-cursor #(merge % conditions))))

(defn update-forecast [state-cursor response]
  (let [units (get-in state-cursor [:config :units])
        forecast (extract-forecast units response)]
    (om/update! state-cursor :forecast forecast)))

(defn conditions-url [config]
  (str "https://api.wunderground.com"
       "/api/"  (config :api-key)
       "/conditions"
       "/lang:" (config :lang)
       "/q/" (config :station-id)
       ".json"))

(defn forecast-url [config]
  (str "https://api.wunderground.com"
       "/api/"  (config :api-key)
       "/hourly10day"
       "/lang:" (config :lang)
       "/q/" (config :station-id)
       ".json"))

(declare fetch-data-periodically)

(defn store-data-and-reschedule [{:keys [state-cursor updater timeout] :as options} response]
  (updater state-cursor response)
  (js/setTimeout #(fetch-data-periodically options) timeout))

(defn fetch-data-periodically [{:keys [state-cursor url-fn] :as options}]
  (GET (url-fn (:config state-cursor))
       {:response-format :json
        :keywords?       true
        :handler         #(store-data-and-reschedule options %)}))