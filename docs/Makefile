publish:
	cd docs;\
	rm -rf _book;\
	gitbook build;\
	cd ..;\
	git checkout gh-pages;\
	rm -rf *
	cp -R docs/_book/* .;\
	git add --all;\
	git commit -am "Update docs";\
	git push -f;\
	git checkout master
serve: 
	gitbook serve	

