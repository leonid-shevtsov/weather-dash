deploy:
	lein clean && lein cljsbuild once min && cd resources/public && python -m SimpleHTTPServer 8000 && cd ../..
