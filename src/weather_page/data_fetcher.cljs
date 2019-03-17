(ns weather-page.data-fetcher
  (:require [ajax.core :refer [GET POST]]
            [om.core :as om]
            [cljs-time.coerce :as tc]
            [cljs-time.local :as tl]
            [weather-page.state :refer [app-cursor]]
            [cljs-time.core :as t]
            [cljs-time.format :as tf]
            [clojure.string :as s]))

(defn float-key [& key-path]
  (fn [hash-map] (js/parseFloat (get-in hash-map key-path))))

(def location-keys
  {:latitude  (float-key :coord :lat)
   :longitude (float-key :coord :lon)})

(defn conditions-keys [units]
  {:time         #(t/to-default-time-zone (tc/from-long (* 1000 (get % :dt))))
   :weather      #(get-in % [:weather 0 :description])
   :icon_code    #(get-in % [:weather 0 :id])
   :is_night     #(s/ends-with? (get-in % [:weather 0 :icon]) "n")

   :temperature  #(.round js/Math (get-in % [:main :temp]))
   :feelslike    #(.round js/Math  (get-in % [:main :temp])) ; TODO calculate feels like?
   :wind         #(.round js/Math (get-in % [:wind :speed]))
   :wind_kph     #(* (if (= units :metric) 3.6 1.61)  (get-in % [:wind :speed])) ; for Beaufort scale
   :wind_degrees #(get-in % [:wind :deg])})

(defn forecast-keys [units]
  {:time          #(t/to-default-time-zone (tc/from-long (* 1000 (get % :dt))))
   :temperature   #(.round js/Math (get-in % [:main :temp]))
   :feelslike     #(.round js/Math (get-in % [:main :temp])) ; TODO calculate feels like?
   :wind          #(get-in % [:wind :speed])
   :precipitation #(/ (get-in % [:rain :3h]) 3)
   :cloudcover    #(get-in % [:clouds :all])
   ; these two are always in metric because I need them for calculation
   ; rather than displays
   :snow-amount   #(/ (get-in % [:snow :3h]) 3)
   :rain-amount   #(/ (get-in % [:rain :3h]) 3)})

(defn extract-keys [data keys]
  (reduce #(assoc %1 (get %2 0) ((get %2 1) data)) {} keys))

(def forecast-limit (/ 72 3)) ; 3-hour intervals to display as forecast

(defn extract-conditions [units response]
  (let [data response
        location (extract-keys data location-keys)
        conditions (extract-keys data (conditions-keys units))]
    {:location location
     :conditions conditions}))

(defn extract-forecast [units response]
  (let [forecast-data (take forecast-limit (:list response))]
    (map #(extract-keys % (forecast-keys units)) forecast-data)))

(defn update-conditions [state-cursor response]
  (let [units (keyword (get-in @state-cursor [:config :units]))
        conditions (extract-conditions units response)]
    (om/transact! state-cursor #(merge % conditions))))

(defn update-forecast [state-cursor response]
  (let [units (keyword (get-in @state-cursor [:config :units]))
        forecast (extract-forecast units response)]
    (om/update! state-cursor :forecast forecast)))

(defn conditions-url [config]
  (str "https://api.openweathermap.org/data/2.5/weather"
       "?lang=" (config :lang)
       "&id=" (config :station-id)
       "&units=" (config :units)
       "&appid=" (config :api-key)))

(defn forecast-url [config]
  (str "https://api.openweathermap.org/data/2.5/forecast"
       "?lang=" (config :lang)
       "&id=" (config :station-id)
       "&units=" (config :units)
       "&appid=" (config :api-key)))

(declare fetch-data-periodically)

(defn delayed-fetch [{:keys [id state-cursor] :as options} timeout]
  (when (get-in @state-cursor [:fetch id])
    (let [timeout-id (js/setTimeout #(fetch-data-periodically options) timeout)]
      (om/update! state-cursor [:fetch-timeout id] timeout-id))))

(defn store-data-and-reschedule [{:keys [state-cursor updater timeout] :as options} response]
  (if-let [error (get-in response [:response :error :description])]
    (om/update! state-cursor :api-error error)
    (do
      (om/transact! state-cursor #(assoc % :error nil :api-error nil))
      (updater state-cursor response)
      (delayed-fetch options timeout))))

(defn handle-errors [{:keys [state-cursor] :as options} _response]
  (om/update! state-cursor :error "Error contacting the OpenWeatherAPI server. Retrying in 1 minute...")
  (delayed-fetch options (* 60 1000)))

(defn fetch-data-periodically [{:keys [url-fn state-cursor] :as options}]
  (GET (url-fn (:config @state-cursor))
    {:response-format :json
     :keywords?       true
     :handler         #(store-data-and-reschedule options %)
     :error-handler   #(handle-errors options %)}))

(defn start-fetching []
  (om/update! app-cursor :fetch {:conditions true :forecast true})
  (fetch-data-periodically {:id :conditions
                            :state-cursor app-cursor
                            :url-fn       conditions-url
                            :updater      update-conditions
                            :timeout      (* 60 5 1000)})
  (fetch-data-periodically {:id :forecast
                            :state-cursor app-cursor
                            :url-fn       forecast-url
                            :updater      update-forecast
                            :timeout      (* 60 30 1000)}))

(defn stop-fetching []
  (om/transact! app-cursor :fetch (fn [fetches]
                                    (zipmap (keys fetches) (repeat false))))
  (doseq [timeout-id (vals (:fetch-timeout @app-cursor))]
    (js/clearTimeout timeout-id)))

(defn can-fetch? []
  (and (not-empty (get-in @app-cursor [:config :api-key]))
       (not-empty (get-in @app-cursor [:config :station-id]))))
