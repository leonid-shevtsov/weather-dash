deploy: build local-server

gh-pages: build commit-to-gh-branch

build:
	lein clean && lein cljsbuild once min

local-server:
	cd resources/public && python -m SimpleHTTPServer 8000 && cd ../..

commit-to-gh-branch:
	git checkout gh-pages; \
	ls | grep -v resources | xargs rm -rf; \
	mv resources/public/* .; \
	rm -rf resources; \
	git add .;\
	git commit -am "releasing gh-pages"; \
	git push; \
	git checkout master

