# Setting up local development environment

* Register an [oauth application with github](https://github.com/settings/applications/new) with the following values
   * Homepage URL - http://localhost:8080/jenkins
   * Authorization callback URL - http://localhost:8080/jenkins/dotci/finishLogin

*  Go to `Manage Jenkins`> `Configure System`
    - Fill out required information under DotCi Configuration

* Install mongodb
* Prepare assets:  `npm run build`
* Run plugin:  `mvn hpi:run`

* Run webpack devserver:   `npm run watch`


    
###Using Docker:

* Install docker: https://docs.docker.com/mac/started/
* Install docker-compose: https://docs.docker.com/compose/install/
* Run build `docker-compose build`
* `docker-compose run --rm plugin npm run build`
* Run plugin: `docker-compose up plugin`
