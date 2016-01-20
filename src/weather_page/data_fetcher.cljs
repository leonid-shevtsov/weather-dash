(ns weather-page.data-fetcher
  (:require [ajax.core :refer [GET POST]]
            [om.core :as om]
            [cljs-time.coerce :as tc]
            [cljs-time.local :as tl]
            [weather-page.state :refer [app-cursor]]))

(defn float-key [& key-path]
  (fn [hash-map] (js/parseFloat (get-in hash-map key-path))))

(def location-keys
  {:latitude  (float-key :observation_location :latitude)
   :longitude (float-key :observation_location :longitude)})

(defn conditions-keys [units]
  {:time        #(tl/from-local-string (get % :observation_time_rfc822))
   :weather     #(get % :weather)
   :icon_url        #(get % :icon_url)
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
   :cloudcover (float-key :sky)
   ; these two are always in metric because I need them for calculation
   ; rather than displays
   :snow-amount (float-key :snow :metric)
   :rain-amount (float-key :qpf :metric)})

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
  (let [units (keyword (get-in @state-cursor [:config :units]))
        conditions (extract-conditions units response)]
    (om/transact! state-cursor #(merge % conditions))))

(defn update-forecast [state-cursor response]
  (let [units (keyword (get-in @state-cursor [:config :units]))
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

(defn delayed-fetch [{:keys [id state-cursor] :as options} timeout]
  (when (get-in @state-cursor [:fetch id])
    (let [timeout-id (js/setTimeout #(fetch-data-periodically options) timeout)]
      (om/update! state-cursor [:fetch-timeout id] timeout-id))))

(defn store-data-and-reschedule [{:keys [state-cursor updater timeout] :as options} response]
  (if-let [error (get-in response [:response :error :description])]
    (om/update! state-cursor :api-error error)
    (do
      (updater state-cursor response)
      (delayed-fetch options timeout))))

(defn handle-errors [{:keys [state-cursor] :as options} _response]
  (om/update! state-cursor :error "Error contacting the WeatherUnderground server. Retrying in 1 minute...")
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
  (and (get-in @app-cursor [:config :api-key]) (get-in @app-cursor [:config :station-id])))