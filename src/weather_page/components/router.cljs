(ns weather-page.components.router
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [weather-page.components.page :refer [page]]
            [weather-page.components.settings :refer [settings]]))

(defcomponent router [{:keys [route] :as data} _owner]
            (render [_]
                    (case route
                      :index (om/build page data)
                      :settings (om/build settings data))))
