(ns weather-page.logic-test
  (:require
    [weather-page.logic :as subj]
    [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-beaufort
  (is (= (subj/kph->beaufort 0) 0))
  (is (= (subj/kph->beaufort 200) 12)))