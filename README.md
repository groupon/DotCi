##DotCi - Jenkins github integration, .ci.yml.
Brings ease of build configuration of cloud ci systems like travisci and ease of runtime environment configuration of docker to jenkins.

![Commit status](docs/screenshots/logos/jenkins.png) **+** ![Commit status](docs/screenshots/logos/github.png) **+** ![Commit status](docs/screenshots/logos/docker.png)
***
 - [Features](#features)
 - [Installation](docs/Installation.md)
 - [Usage](docs/Usage.md)
 - [.ci.yml examples](docs/Examples.md)
 - [Extending DotCi](docs/Extending.md)
 - [Developer Setup](docs/DevelopmentSetup.md)
 - [Extras](#extras)
 - [License](#license)

###Features
***
 * **Github Integration** (both github.com and GHE)
    - Automatically sets up Github webhooks for pull requests and github pushes when a new job is setup.
    - Sets [commit status](https://github.com/blog/1227-commit-status-api) during and after build run.
      ![Commit status](docs/screenshots/commit-status.png)
    - Projects are name-spaced under organization which allows creation of multiple projects with same names under different organizations.


 * **Build configuration through .ci.yml**  
    * Speed up builds by running builds in parallel.([Example](docs/Examples.md#parallelization))
    * Configure build environment (language/version/dbs etc). ([Example](docs/Examples.md#build-environment-configuration))
    * Branch/pusher specific build customization through groovy templating.([Example](docs/Examples.md#build-templating))
    * Plugin configuration.([Example](docs/Examples.md#plugin-configuration))
    * Notification configuration.
    * Skip Builds based on sha/branch/pusher/pull request etc.([Example](docs/Examples.md#build-skipping))


 * **Docker Support**
    * Having a Dockerfile in the repo will build an image and run tests against the image.
    * Or specify a docker image to run build against in .ci.yml. ([Example](docs/Examples.md#docker))
    * Link against services like mysql, redis ect. ([Example](docs/Examples.md#docker))

 * **Defaults** for each language type (eg: ``mvn install`` for java), language is auto detected.

 * **Scaling Jenkins**.
   * Backed by a mongodb database
   * No need to purge builds to improve startup time/performance.
   * Query build statistics by querying database.
 * **Build shortcuts for deploy/command line tools**
   * Fetch builds by git sha (`job/meow/23/sha?value=<sha>`)
   * Or branch specific permalinks (`lastSuccessfulMaster`) .

 * **Extensible**
   * Add new types of notifications/plugins supported in .ci.yml by writing plugins for DotCi.

 * **UI enhancements**
   * Organization View

     ![Org](docs/screenshots/org-view.png)
   * User builds view (This is the default view in our jenkins installation)

     ![User Builds](docs/screenshots/user-view.png)
   * Build history by branch

     ![branch history](docs/screenshots/branch-view.png)

###Setup
   See:  [Installation](docs/Installation.md)
###Usage
   See: [Usage](docs/Usage.md)
###.ci.yml examples
   See: [.ci.yml examples](docs/Examples.md)
###Extending DotCi
 See: [Extending DotCi](docs/Extending.md)
###Developer Setup
  See: [Developer Setup](docs/DevelopmentSetup.md)
###Extras  
  * Branch Specific build status badges using [Embeddable Build Status Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Embeddable+Build+Status+Plugin).
     Add `branch=<branch-name>` param to build status url,  eg `https://ci.example.com/job/myorg/job/railsapp/badge/icon?branch=master`

  * [Quick setup guide for digitalocean](https://github.com/groupon/DotCi/wiki/Setup-Cloud-CI-in-15-minutes-with-Jenkins,-DotCi,-Docker-and-DigitalOcean).   

###License
```
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
