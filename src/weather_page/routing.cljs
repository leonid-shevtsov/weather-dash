(ns weather-page.routing
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [secretary.core :as sec :include-macros true]
            goog.events
            [weather-page.state :refer [app-cursor]])
  (:import
    [goog History]
    [goog.history Html5History EventType]))


(aset js/goog.history.Html5History.prototype "getUrl_"
      (fn [token]
        (this-as this
          (if (.-useFragment_ this)
            (str "#" token)
            (str (.-pathPrefix_ this) token)))))

(defn get-token []
  (if (Html5History.isSupported)
    (str js/window.location.pathname js/window.location.search)
    (if (= js/window.location.pathname "/")
      (.substring js/window.location.hash 1)
      (str js/window.location.pathname js/window.location.search))))

(defn make-history []
  (if (Html5History.isSupported)
    (doto (Html5History.)
      (.setPathPrefix (str js/window.location.protocol
                           "//"
                           js/window.location.host))
      (.setUseFragment false))
    (if (not= "/" js/window.location.pathname)
      (aset js/window "location" (str "/#" (get-token)))
      (History.))))

(defn handle-url-change [e]
  ;; log the event object to console for inspection
  (js/console.log e)
  ;; and let's see the token
  (js/console.log (str "Navigating: " (get-token)))
  ;; we are checking if this event is due to user action,
  ;; such as click a link, a back button, etc.
  ;; as opposed to programmatically setting the URL with the API
  (when-not (.-isNavigation e)
    ;; in this case, we're setting it
    (js/console.log "Token set programmatically")
    ;; let's scroll to the top to simulate a navigation
    (js/window.scrollTo 0 0))
  ;; dispatch on the token
  (sec/dispatch! (get-token)))

(defonce history (doto (make-history)
                   (goog.events/listen EventType.NAVIGATE
                                       ;; wrap in a fn to allow live reloading
                                       #(handle-url-change %))
                   (.setEnabled true)))

(defn nav! [token]
  (.setToken history token))


(defonce routes
         (do
            (sec/defroute index-page "/" []
                          (om/update! app-cursor :route :index))

            (sec/defroute settings-page "/settings" []
                          (om/update! app-cursor :route :settings))))
