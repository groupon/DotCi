**Installation Steps**
 1. [Install Plugin](#install-plugin)
 2. [Mongodb setup](#mongodb-setup)
 3. [Github oauth plugin configuration](#github-oauth-plugin-setup)
 4. [DotCi plugin configuration](#dotci-plugin-configuration)
 5. [Configure package management](#configure-package-management)

## Install Plugin 
  - Install DotCi plugin from the update center. This should also install [github-oauth-plugin](https://wiki.jenkins-ci.org/display/JENKINS/Github+OAuth+Plugin)

## Mongodb setup
   Install [mongodb](https://www.mongodb.org/) accessible to your jenkins instance.

## Github oauth plugin configuration
   * Register an OAuth [application](https://github.com/settings/applications/new) with github to obtain Client-ID/Secret.
   
   * Go to 'Manage Jenkins' > 'Configure Global Security' 
     Under 'Security Realm' select 'Github Authentication Plugin' and fill out required oauth credentials.


## DotCi plugin configuration
  Got to 'Manage Jenkins'> 'Configure System' and fill out required information under DotCi Configuration
   
![dotci setup](/screenshots/dotci-plugin-configuration.png)

     
## Configure package management
  
There are two options for installing packages that the build needs,
    
- **Docker** - Install docker on jenkins slaves where builds would run.
- **Non-Docker** - DotCi calls a shell function 'install_packages' with requested packages and languages specified in the enviroment
section of .ci.yml. It is upto you to provide implementation of this shell function available to jenkins slave running dotci build.
   
   
     
    
 
