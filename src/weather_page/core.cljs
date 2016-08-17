(ns weather-page.core
  (:require [om.core :as om :include-macros true]
            [weather-page.state :refer [app-state app-cursor]]
            [weather-page.components.router :refer [router]]
            [weather-page.data-fetcher :refer [can-fetch? start-fetching]]))

(enable-console-print!)

(om/root router app-state {:target (.getElementById js/document "app") :shared {:config (get app-cursor :config)}})

; Must only happen after om/root has been called
(defonce fetch-default
         (when (can-fetch?) (start-fetching)))

; FIXME lags; maybe find better way to do it, but it's not critical
;(defonce update-window-size
;         (.addEventListener js/window "resize"  #(om/update! app-cursor :page {:width (.-innerWidth js/window) :height (.-innerHeight js/window)}) 1))


