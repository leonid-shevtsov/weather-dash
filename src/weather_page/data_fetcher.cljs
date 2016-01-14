(ns weather-page.data-fetcher
  (:require [ajax.core :refer [GET POST]]
            [om.core :as om])
  (:require-macros [weather-page.config :refer [config]]))

(def conditions-url (str "https://api.wunderground.com/api/" (config :api-key) "/conditions/lang:" (config :lang) "/q/" (config :station-id) ".json"))
(def forecast-url (str "https://api.wunderground.com/api/" (config :api-key) "/hourly10day/lang:" (config :lang) "/q/" (config :station-id) ".json"))

(declare fetch-data-periodically)

(defn store-data-and-reschedule [{:keys [cursor key timeout] :as options} response]
  (om/update! cursor (get response key))
  (js/setTimeout #(fetch-data-periodically options) timeout))

(defn fetch-data-periodically [{:keys [url] :as options}]
  (GET url {:response-format :json
                 :keywords? true
                 :handler #(store-data-and-reschedule options %)}))