##DotCi - Jenkins github integration, .ci.yml.
Brings ease of build configuration of cloud ci systems like travisci and ease of runtime environment configuration of docker to jenkins.


### Docker Based Build Environments
  - Use [Docker-Compose](usage/DockerBuild.md) for build setup.

###Github Integration
  - Automatically sets up Github [webhooks](https://help.github.com/articles/about-webhooks/) for pull requests and github pushes when a new job is setup.
  - Private Repo support via generated [deploy keys](https://developer.github.com/guides/managing-deploy-keys/).
  - Sets [commit status](https://github.com/blog/1227-commit-status-api) during and after build run.
  - Jobs follow name-spacing structure of github, jobs are nested under organization folder([Screenshot](screenshots/org-view.png))
  - **Authorization** scheme mapped to corresponding github repo permissions.
   ([Screenshot](screenshots/authorization.png))
  -  Changeset is calculated  from github payload.
  -  Builds for master branch get special treatment
      * `LastBuild` permalink points to last build for master.
      * `master` branch status is treated as job status( useful for dashboard/job weather tools)
      * `lastSuccessfulMaster` permalink.

### New Jenkins UI
  - See [UI](usage/UI.md)

### Job Enhancements
  * Custom build history branch tabs. 
   - Fetch builds by git sha (`job/meow/sha?value=<sha>`)
   - Fetch last build for a branch (`job/meow?branch=<branch-name>`)
  * Scaling Jenkins.
   - Backed by a mongodb database.
   - Store unlimited number of builds without performance degradation.
   - Query build statistics by querying database. Job/Build data is stored in mongodb bson format.
  * User builds view (This is the default view in our jenkins installation) ([Screenshot](screenshots/user-view.png))
