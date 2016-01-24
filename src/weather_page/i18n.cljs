(ns weather-page.i18n
  (:require [taoensso.tower :as tower :refer-macros (with-tscope)]
            [weather-page.state :refer [app-state]]))

(def ^:private tconfig
  {:fallback-locale :en
   :compiled-dictionary (tower/dict-compile* "translations.clj")})

(def t #(apply (tower/make-t tconfig) (keyword (.toLowerCase (get-in @app-state [:config :lang]))) %&))
