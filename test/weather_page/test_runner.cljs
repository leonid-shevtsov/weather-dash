(ns weather-page.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            weather-page.logic-test
            weather-page.data-fetcher-test))

(doo-tests 'weather-page.logic-test
           'weather-page.data-fetcher-test)