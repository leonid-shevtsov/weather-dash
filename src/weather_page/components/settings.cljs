(ns weather-page.components.settings
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :refer-macros [html]]
            [weather-page.routing :refer [nav!]]
            [weather-page.state :refer [app-state]]
            [weather-page.data-fetcher :refer [start-fetching stop-fetching]]
            [weather-page.local-storage :refer [write-config]]
            [weather-page.i18n :refer [t]]))

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
           [:h1 (t :settings/title)]
           [:.form-label [:label (t :settings/api-key)]]
           [:div [:input {:value api-key :on-change (input-updater-fn config :api-key)}]]
           [:.form-label [:label (t :settings/station-id)]]
           [:div [:input {:value station-id :on-change (input-updater-fn config :station-id)}]]
           [:.form-label [:label (t :settings/language)]]
           [:div
            [:select {:value lang :on-change (input-updater-fn config :lang)}
             [:option {:value "EN"} (t :settings/lang-en)]
             [:option {:value "RU"} (t :settings/lang-ru)]]]
           [:.form-label [:label (t :settings/units)]]
           [:div
            [:input {:type      "radio"
                     :name      "units"
                     :value     "metric"
                     :checked   (= units "metric")
                     :on-change (input-updater-fn config :units)}] " " (t :settings/metric) " "
            [:input {:type      "radio"
                     :name      "units"
                     :value     "imperial"
                     :checked   (= units "imperial")
                     :on-change (input-updater-fn config :units)}] " " (t :settings/imperial) " "]
           [:.form-label [:button {:on-click apply-settings} (t :settings/apply-settings)]]])))
