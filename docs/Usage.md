**Table of Contents**
- [Setup a new DotCi Job](#setup-a-new-dotci-job)
- [Overriding build type](#override-default-build-type)
- [Build url shortcuts](#build-url-shortcuts)
- Build Type Usage
  * [Docker Build](DockerBuild.md)
  * [Install_Packages Build](InstallPackages.md)

## Setup a new DotCi Job
   - Click new DotCi job on side-panel

      ![New Job](screenshots/new-job-link.png)  
   - Select Github Org and click new job

     ![org](screenshots/create-job.png)

## Override default build type
  In job configuration page

  ![override build type](screenshots/override-build-type.png)
## Build Url shortcuts
 * Fetch builds by git sha ( `job/meow/23/sha?value=<sha>`)
 * Or branch specific permalinks (`lastSuccessfulMaster`) .
     
