**Table of Contents**
- [Setup a new DotCi Job] (#setup-a-new-dotci-job)
- [.ci.yml reference] (#ciyml-reference)
   - [Environment Section](#environment-section)
   - [Build Section](#build-section)
   - [Notifications Section](#notifications-section)
   - [Plugins Section](#plugins-section)
- [Templating .ci.yml] (#templating-ciyml)   
   - [Environment Variables](#environment-variables)
   - [Groovy templating](#groovy-templating )
- [Build url shortcuts](#build-url-shortcuts)


## Setup a new DotCi Job
   - Click new DotCi job on side-panel
     
      ![New Job](screenshots/new-job-link.png)  
   - Select Github Org and click new job
    
     ![org](screenshots/create-job.png)

## .ci.yml reference
 Build automatically inherits .ci.yml based on language that is autodetected. 
 
 Check in a .ci.yml with **overrides** for overriding the [defaults](../src/main/resources/com/groupon/jenkins/buildconfiguration/base_yml)
 
.ci.yml file is divided into four major sections

#### Environment Section 
  
 -  Non-docker builds
```yaml
 environment:
    vars: #These vars are exported in shell before build starts
       BUNDLE_WITHOUT: production:development  
     
     language: ruby     # always single value
     
     language_versions:  # single value or list of values (extra row in build matrix)
     - ree-1.8.7
     - mri-1.8.7
   
     packages: #extra arguments that passed into install_packages call
      - memcached-1.4.5 
```

 - Docker builds
```yaml
 environment:
     vars: #These vars are exported as docker env variables
       BUNDLE_WITHOUT: production:development 
     language: ruby     # always single value   
     image:< docker/image-name> # Optional if repo has Dockerfile which would be used to  build an image
     services: #list or single value 
        - acme/mongo:1.4
        - acme/redis:2.4
```
   Images specified under `services` section would be [linked](http://docs.docker.io/use/working_with_links_names/) to 
   the container specified with `image` ( or `Dockerfile`)
   
#### Build Section

```yaml
build:
  skip: true #skips build
  before: # single command or list of commands (run serially on each matrix job)
  - gem install -y rubygems-update
  - update_rubygems

  info: #print machine/environment diagnostics
  - bundle --version

  # single command or list of commands (run serially on each matrix job)
  #run: rake test
  # or as a hash map (each entry run in parallel in build matrix)
  run:         
    unit:        bundle exec rake spec:units
    integration: bundle exec rake spec:integration
    acceptance:  # Each parallelized step can also have multiple serial commands
    - bundle exec rake cucumber
    - bundle exec rake cucumber:javascript

  #starts a new build - initializes environment and  runs this script
  after: cap deploy staging
```
#### Notifications Section 
```yaml
notifications: #list of notifications, notified ONLY on build fail and branch recovery.
  - email: #as always it can be single value or a list
     - kittah@gmail.com
     - kitty@gmail.com
  - hipchat: room-name      # single or list of roomids  
  - campfire: Devops              
```

#### Plugins Section 
```yaml
plugins: 
  - test_output: 
     format: tap |junit
  - artifacts: blah.txt # this needs to be ant file specifier format ( see  http://ant.apache.org/manual/Types/fileset.html)
  # configure your project's Build Environment: "Delete workspace before build starts" to avoid accumulative artifacts
  - checkstyle #expects file to be target/checkstyle-result.xml
  - cobertura #expects target/site/cobertura/coverage.xml
  - findbugs #expects target/findbugsXml.xml
```

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

For example, an if statement can be used to choose a ruby version based on a branch.

``` yaml
environment:
 language: ruby
 language_versions: 
  <% if( DOTCI_BRANCH != 'migration') { %>
   - '1.9.2_p290' 
   <% } %>
   - '1.9.3_p290'
```

or send extra notification to yourself when a build fails

```yaml
notifications:
  <% if( DOTCI_PUSHER == 'joe') { %>
   - sms: 1234344453
   <% } %>

```

## Build Url shortcuts
 * Fetch builds by git sha ( `job/meow/23/sha?value=<sha>`) 
 * Or branch specific permalinks (`lastSuccessfulMaster`) .
     
