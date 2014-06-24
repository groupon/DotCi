# Setting up local development environment

* Install mongodb
* Register an [oauth application with github](https://github.com/settings/applications/new) with the following values
   * Homepage URL - http://127.0.0.1:8080
   * Authorization callback URL - http://127.0.0.1:8080/jenkins/securityRealm/finishLogin
* Launch local instance with `mvn hpi:run`
*  Go to `Manage Jenkins` > `Configure Global Security`
  Under `Security Realm` select `Github Authentication Plugin` and fill out required oauth credentials.  
*   Got to `Manage Jenkins`> `Configure System` and fill out required information under DotCi Configuration  
* Optionally add a noop `install_packages` in your `.bash_profile`
   ```bash
   install_packages(){  
     echo "installing packages"
   }
    ```
