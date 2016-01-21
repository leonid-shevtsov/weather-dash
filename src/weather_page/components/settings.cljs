(ns weather-page.components.settings
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [weather-page.routing :refer [nav!]]
            [weather-page.state :refer [app-state]]
            [weather-page.data-fetcher :refer [start-fetching stop-fetching]]
            [weather-page.local-storage :refer [write-config]]))

(defn input-updater-fn [cursor path]
  (fn [event]
    (om/update! cursor path (.. event -target -value))))

(defn apply-settings [event]
  (.preventDefault event)
  (write-config (:config @app-state))
  (stop-fetching)
  (start-fetching)
  (nav! "/"))

(defcomponent settings [{{:keys [api-key station-id lang units] :as config} :config} _owner]
  (render [_]
    (html [:form
           [:h1 "Settings"]
           [:.form-label [:label "API key"]]
           [:div [:input {:value api-key :on-change (input-updater-fn config :api-key)}]]
           [:.form-label [:label "Station ID"]]
           [:div [:input {:value station-id :on-change (input-updater-fn config :station-id)}]]
           [:.form-label [:label "Language"]]
           [:div
            [:select {:value lang :on-change (input-updater-fn config :lang)}
             [:option {:value "EN"} "English"]
             [:option {:value "RU"} "Russian"]]]
           [:.form-label [:label "Units"]]
           [:div
            [:input {:type      "radio"
                     :name      "units"
                     :value     "metric"
                     :checked   (= units "metric")
                     :on-change (input-updater-fn config :units)}] " Metric "
            [:input {:type      "radio"
                     :name      "units"
                     :value     "english"
                     :checked   (= units "english")
                     :on-change (input-updater-fn config :units)}] " Imperial "]
           [:.form-label [:button {:on-click apply-settings} "Apply settings"]]])))
