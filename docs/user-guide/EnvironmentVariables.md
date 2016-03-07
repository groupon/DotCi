## Environment Variables

In addition to [Jenkins build
variables](https://wiki.jenkins-ci.org/display/JENKINS/Building+a+software+project#Buildingasoftwareproject-JenkinsSetEnvironmentVariables),
DotCi provides the following global variables:

* `DOTCI_BRANCH` -  current branch
* `DOTCI` - always true
* `CI` - always true
* `DOTCI_SHA` - current sha being built
* `GIT_URL` - git url
* `DOTCI_PUSHER` - github username whose git push triggred this build
* `DOTCI_PULL_REQUEST` - pull request number being built
* `DOTCI_PULL_REQUEST_SOURCE_BRANCH` - the pull request branch
* `DOTCI_PULL_REQUEST_TARGET_BRANCH` - the branch the pull request is targeting
