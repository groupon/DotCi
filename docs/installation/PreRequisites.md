# Pre-Requisites

## Mongo DB

Install [mongodb](https://www.mongodb.org/) in a location accessible to your Jenkins instance.


## Github Applications

Register an [OAuth
Application](https://github.com/settings/applications/new) with GitHub
to generate __Client ID__ and __Client Secret__. 
The __Authorization callback URL__ needs to be `http://<YOUR-JENKINS-URL>/dotci/finishLogin`

Register a separate [OAuth
Application](https://github.com/settings/applications/new) with GitHub
to generate __Client ID__ and __Client Secret__. 
The __Authorization callback URL__ needs to be `http://<YOUR-JENKINS-URL>/securityRealm/finishLogin`. 
This will later be used in conjuction to enable  [Matrix-based+security](https://wiki.jenkins-ci.org/display/JENKINS/Matrix-based+security).

[configure-dotci.groovy](https://github.com/DotCi/jenkinsci-dotci-example/blob/master/configure-dotci.groovy) is a sample configuration using [Jenkins+Script+Console](https://wiki.jenkins-ci.org/display/JENKINS/Jenkins+Script+Console) to apply both values above and more.
