## 1.3.0(07/15/2014)
 Bugfixes: 
   - Call `onLoad` on build load. Not calling this was causing junit action to fail.
 
 Features: 
   - New DotCi Authorization which maps github org/repo permissions to OrgContainers and DotCiJobs.

## 1.2.2 (07/10/2014)

Bugfixes: 
  - PR checking typo fix.
  
## 1.2.1 (07/10/2014)

Features: 
 - Linked docker container mapped to localhost if socat utility is installed.

Bugfixes: 
  - DotCi vars are set to null if they are missing.
 
## 1.2.0 (06/25/2014)

Features:
 - Add autocomplete for branch parameter textbox
 - Point `getLastBuild()` to `master` branch.

Bugfixes: 
  - Fix bundle caching for ruby based on version(#33)

## 1.1.1 (06/17/2014)

Bugfixes: 
 - Add `read:repo` scope to github oauth
 - update docker command options to new style
