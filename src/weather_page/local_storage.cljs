(ns weather-page.local-storage)

(defn read-config []
  (if-let [config-str (.getItem js/localStorage "config")]
    (js->clj (.parse js/JSON config-str) :keywordize-keys true)
    nil))

(defn write-config [config]
  (.setItem js/localStorage "config" (.stringify js/JSON (clj->js config))))
