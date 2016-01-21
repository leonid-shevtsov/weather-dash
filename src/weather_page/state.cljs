(ns weather-page.state
  (:require [om.core :as om]
            [weather-page.local-storage :refer [read-config]]))

(def default-config {:api-key    ""
                     :lang       "RU"
                     :station-id ""
                     :units      "metric"})

(defonce app-state (atom {:route         :index
                          :config        (or (read-config) default-config)
                          :fetch         {}
                          :fetch-timeout {}
                          :location      {:latitude  0
                                          :longitude 0}
                          :conditions    {:time        nil
                                          :weather     ""
                                          :temperature 0
                                          :feelslike   0
                                          :wind        0
                                          :wind_kph    0}
                          :forecast      []}))

(defonce app-cursor (om/root-cursor app-state))