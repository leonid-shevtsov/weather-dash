(ns weather-page.condition-icons (:require [clojure.string :as s]))

(def condition-icons
  {
    200	"thunderstorm" ; Thunderstorm	thunderstorm with light rain	 11d
    201	"thunderstorm" ; Thunderstorm	thunderstorm with rain	 11d
    202	"thunderstorm" ; Thunderstorm	thunderstorm with heavy rain	 11d
    210	"thunderstorm" ; Thunderstorm	light thunderstorm	 11d
    211	"thunderstorm" ; Thunderstorm	thunderstorm	 11d
    212	"thunderstorm" ; Thunderstorm	heavy thunderstorm	 11d
    221	"thunderstorm" ; Thunderstorm	ragged thunderstorm	 11d
    230	"thunderstorm" ; Thunderstorm	thunderstorm with light drizzle	 11d
    231	"thunderstorm" ; Thunderstorm	thunderstorm with drizzle	 11d
    232	"thunderstorm" ; Thunderstorm	thunderstorm with heavy drizzle	 11d
    300	"sprinkle" ; Drizzle	light intensity drizzle	 09d
    301	"sprinkle" ; Drizzle	drizzle	 09d
    302	"sprinkle" ; Drizzle	heavy intensity drizzle	 09d
    310	"sprinkle" ; Drizzle	light intensity drizzle rain	 09d
    311	"sprinkle" ; Drizzle	drizzle rain	 09d
    312	"sprinkle" ; Drizzle	heavy intensity drizzle rain	 09d
    313	"showers" ; Drizzle	shower rain and drizzle	 09d
    314	"showers" ; Drizzle	heavy shower rain and drizzle	 09d
    321	"showers" ; Drizzle	shower drizzle	 09d
    500	"rain" ; Rain	light rain	 10d
    501	"rain" ; Rain	moderate rain	 10d
    502	"rain" ; Rain	heavy intensity rain	 10d
    503	"rain" ; Rain	very heavy rain	 10d
    504	"rain" ; Rain	extreme rain	 10d
    511	"rain" ; Rain	freezing rain	 13d
    520	"rain" ; Rain	light intensity shower rain	 09d
    521	"showers" ; Rain	shower rain	 09d
    522	"showers" ; Rain	heavy intensity shower rain	 09d
    531	"showers" ; Rain	ragged shower rain	 09d
    600	"snow" ; Snow	light snow	 13d
    601	"snow" ; Snow	Snow	 13d
    602	"snow" ; Snow	Heavy snow	 13d
    611	"sleet" ; Snow	Sleet	 13d
    612	"sleet" ; Snow	Light shower sleet	 13d
    613	"sleet" ; Snow	Shower sleet	 13d
    615	"rain-mix" ; Snow	Light rain and snow	 13d
    616	"rain-mix" ; Snow	Rain and snow	 13d
    620	"snow" ; Snow	Light shower snow	 13d
    621	"snow" ; Snow	Shower snow	 13d
    622	"snow" ; Snow	Heavy shower snow	 13d
    701	"fog" ; Mist	mist	 50d
    711	"smoke" ; Smoke	Smoke	 50d
    721	"day-haze" ; Haze	Haze	 50d
    731	"dust" ; Dust	sand/ dust whirls	 50d
    741	"fog" ; Fog	fog	 50d
    751	"sandstorm" ; Sand	sand	 50d
    761	"dust" ; Dust	dust	 50d
    762	"volcano" ; Ash	volcanic ash	 50d
    771	"strong-wind" ; Squall	squalls	 50d
    781	"tornado" ; Tornado	tornado	 50d
    800	"day-clear"; Clear	clear sky	 01d  01n
    801	"day-overcast" ; Clouds	few clouds: 11-25%	 02d  02n
    802	"day-cloudy" ; Clouds	scattered clouds: 25-50%	 03d  03n
    803	"day-cloudy-high" ; Clouds	broken clouds: 51-84%	 04d  04n
    804	"cloudy" ; Clouds	overcast clouds: 85-100%
  })

(defn condition-icon [icon-code is-night]
  (when icon-code
      (let [icon-name (get condition-icons icon-code "alien")
            icon-timeofday (if is-night (s/replace icon-name "day" "night"))]
            icon-timeofday)))