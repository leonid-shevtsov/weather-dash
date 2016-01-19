(ns weather-page.state
  (:require [om.core :as om])
  (:require-macros [weather-page.config :refer [config]]))

(defonce app-state
         (atom {:route   :index
                :config {:api-key    (config :api-key)
                         :lang       (config :weather-lang)
                         :station-id (config :station-id)
                         :units      "metric"}}
               {:location   {:latitude  0
                             :longitude 0}
                :conditions {:time        nil
                             :weather     ""
                             :temperature 0
                             :feelslike   0
                             :wind        0
                             :wind_kph    0}
                :forecast   []}))

(defonce app-cursor (om/root-cursor app-state))