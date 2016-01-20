(ns weather-page.state
  (:require [om.core :as om]
            [alandipert.storage-atom :refer [local-storage]]))

(def default-state {:route   :index
                    :config {:api-key    ""
                             :lang       "RU"
                             :station-id ""
                             :units      "metric"}
                    :fetch {}
                    :fetch-timeout {}
                    :location   {:latitude  0
                                 :longitude 0}
                    :conditions {:time        nil
                                 :weather     ""
                                 :temperature 0
                                 :feelslike   0
                                 :wind        0
                                 :wind_kph    0}
                    :forecast   []})

(defonce app-state (local-storage (atom default-state) :app-state))
(defonce app-cursor (om/root-cursor app-state))