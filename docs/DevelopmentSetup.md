# Setting up local development environment

* Register an [oauth application with github](https://github.com/settings/applications/new) with the following values
   * Homepage URL - http://127.0.0.1:8080
   * Authorization callback URL - http://127.0.0.1:8080/jenkins/securityRealm/finishLogin
* Install pre-requisites:
  * Non-docker:
    * Install mongodb
  * Docker:
    * Install docker: https://docs.docker.com/mac/started/
    * Install docker-compose: https://docs.docker.com/compose/install/
    * Run build `docker-compose build`
* Run plugin
  * Non-docker: `mvn hpi:run`
  * Docker: `docker-compose up plugin`
*  Go to `Manage Jenkins` > `Configure Global Security`
  * Under `Security Realm` select `Github Authentication Plugin` and fill out required oauth credentials.
*  Go to `Manage Jenkins`> `Configure System`
  * Fill out required information under DotCi Configuration
* Optionally add a noop `install_packages` in your `.bash_profile`:
  ```bash
  install_packages(){
    echo "installing packages"
  }
  ```
