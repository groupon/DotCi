publish:
	rm -rf site;\
	mkdocs build;\
	git checkout gh-pages;\
	cp -R site/* .;\
	git add --all;\
	git commit -am "Update docs";\
	git push -f;\
	git checkout master
serve: 
	mkdocs serve	

