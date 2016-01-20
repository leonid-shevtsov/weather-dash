(ns weather-page.core
  (:require [om.core :as om :include-macros true]
            [weather-page.state :refer [app-state app-cursor]]
            [weather-page.components.router :refer [router]]
            [weather-page.data-fetcher :refer [can-fetch? start-fetching]]))

(enable-console-print!)

(om/root router app-state {:target (.getElementById js/document "app")})

; Must only happen after om/root has been called
(defonce fetch-default
         (when (can-fetch?) (start-fetching)))