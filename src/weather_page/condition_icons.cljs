(ns weather-page.condition-icons)

(def condition-icons
  {"chanceflurries" "day-snow"
   "chancerain" "day-rain"
   "chancesleet" "day-sleet"
   "chancesnow" "day-snow"
   "chancetstorms" "day-lightning"
   "clear" "day-sunny"
   "cloudy" "cloudy"
   "flurries" "snow"
   "fog" "fog"
   "hazy" "day-haze"
   "mostlycloudy" "day-cloudy"
   "mostlysunny" "day-cloudy"
   "nt_chanceflurries" "night-alt-snow"
   "nt_chancerain" "night-alt-rain"
   "nt_chancesleet" "night-alt-sleet"
   "nt_chancesnow" "night-alt-snow"
   "nt_chancetstorms" "night-alt-lightning"
   "nt_clear" "night-clear"
   "nt_cloudy" "cloudy"
   "nt_flurries" "snow"
   "nt_fog" "fog"
   "nt_hazy" "dust"
   "nt_mostlycloudy" "night-alt-cloudy"
   "nt_mostlysunny" "night-alt-cloudy"
   "nt_partlycloudy" "night-alt-cloudy"
   "nt_partlysunny" "night-alt-cludy"
   "nt_rain" "rain"
   "nt_sleet" "sleet"
   "nt_snow" "snow"
   "nt_sunny" "night-clear"
   "nt_tstorms" "night-alt-lightning"
   "partlycloudy" "day-cloudy"
   "partlysunny" "day-cloudy"
   "rain" "rain"
   "sleet" "sleet"
   "snow" "snow"
   "sunny" "day-sunny"
   "tstorms" "day-lightning"})

(defn condition-icon [icon-url]
  (when icon-url
    (let [match (.match icon-url #"/([^/]+)\.gif$")
          icon-name (get match 1)]
      (get condition-icons icon-name "alien"))))