(defproject weather-page "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.clojure/core.async "0.2.374"]
                 [sablono "0.3.6"]
                 [org.omcljs/om "0.9.0"]
                 [prismatic/om-tools "0.3.12"]
                 [cljs-ajax "0.5.3"]
                 [cljsjs/highcharts "4.1.10-1"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [secretary "1.2.3"]
                 [environ "1.0.1"]]

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-figwheel "0.5.0-3"]
            [lein-doo "0.1.6"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                ;:figwheel {:on-jsload "weather-page.core/on-js-reload"}

                :compiler {:main weather-page.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/weather_page.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/weather_page.js"
                           :main weather-page.core
                           :optimizations :advanced
                           :pretty-print false}}
               {:id "test"
                :source-paths ["src" "test"]
                :compiler {:output-to "resources/public/js/out/testable.js"
                           :main weather-page.test-runner
                           :optimizations :none}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             :server-port 3450
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             }
  :doo {:build "test"})

