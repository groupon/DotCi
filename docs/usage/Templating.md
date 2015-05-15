- [Templating .ci.yml](#templating-ciyml)
   - [Environment Variables](#environment-variables)
   - [Groovy templating](#groovy-templating )
## Templating .ci.yml

#### Environment Variables
In addition to [Jenkins build variables](https://wiki.jenkins-ci.org/display/JENKINS/Building+a+software+project#Buildingasoftwareproject-JenkinsSetEnvironmentVariables), DotCi provides the following global variables  
- ```DOTCI_BRANCH```  current branch
- ```DOTCI``` always true
- ```CI``` always true
- ```DOTCI_SHA``` current  sha being built
- ```GIT_URL``` git url
- ```DOTCI_PUSHER``` github username whose git push triggred this build
- ```DOTCI_PULL_REQUEST``` pull request number being built

#### Groovy templating  

`.ci.yml` is a [groovy template](http://groovy.codehaus.org/Groovy+Templates) which is  run through a groovy preprocessor before build starts.

Send extra notification to yourself when a build fails

```yaml
notifications:
  <% if( DOTCI_PUSHER == 'joe') { %>
   - sms: 1234344453
   <% } %>

```
