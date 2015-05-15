publish:
	gitbook build
	cd ..;\
	git checkout gh-pages;\
	cp -R docs/_book/* .;\
	git add --all;\
	git commit -am "Update docs";\
	git push;\
	git checkout master

