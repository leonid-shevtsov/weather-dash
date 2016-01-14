(ns weather-page.data-fetcher-test
  (:require
    [weather-page.data-fetcher :as subj]
    [weather-page.test-fixtures :as fixtures]
    [cljs-time.coerce :as tc]
    cljs-time.extend
    [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-float-key
  (is (= ((subj/float-key :foo) {:foo "0.123"}) 0.123)))

(deftest test-extract-keys
  (is (= ((subj/extract-keys {:foo "bar" :baz 0}
                             {:foo identity :baz inc})
           {:foo "bar" :baz 1}))))

(deftest test-extract-conditions
  (is (= (subj/extract-conditions :metric fixtures/conditions-response)
         {:location {:latitude 48.407410
                     :longitude 35.075974}
          :conditions {:time (tc/from-string "Sat, 16 Jan 2016 22:00:39 +0200")
                       :weather "Fog"
                       :temperature 2.6
                       :feelslike 3
                       :wind 0
                       :wind_kph 0}}))
  (is (= (subj/extract-conditions :english fixtures/conditions-response)
         {:location {:latitude 48.407410
                     :longitude 35.075974}
          :conditions {:time (tc/from-string "Sat, 16 Jan 2016 22:00:39 +0200")
                       :weather "Fog"
                       :temperature 36.7
                       :feelslike 37
                       :wind -9999
                       :wind_kph 0}})))

(deftest test-extract-forecast
  (is (= (count (subj/extract-forecast :metric fixtures/hourly10day-response))
         72))

  (is (= (first (subj/extract-forecast :metric fixtures/hourly10day-response))
         {:time          (tc/from-long 1452978000000)
          :temperature   1
          :feelslike     -2
          :wind          8
          :precipitation 0
          :cloudcover    98}))

  (is (= (first (subj/extract-forecast :english fixtures/hourly10day-response))
         {:time          (tc/from-long 1452978000000)
          :temperature   34
          :feelslike     29
          :wind          5
          :precipitation 0
          :cloudcover    98})))
