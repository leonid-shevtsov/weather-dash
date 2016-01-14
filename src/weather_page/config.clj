; Borrowed from https://github.com/asciinema/asciinema-player/commit/facc64737155765200f8dcba5dc2a0fe58e65fc7

(ns weather-page.config
  (:require [environ.core :refer [env]]))

(defmacro config [param]
  (get env param))
