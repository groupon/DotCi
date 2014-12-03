##2.4.0(01/12/2014)
Bugfix: 
 - Fix docker link container cleanup bug where linked containers were not being discarded if the build failed.
 - Store empty lists in database.

##2.3.0(11/24/2014)
Features:
 - Add Commit History View for the `SHA` in current build.
 - Add support for private repos.
   * Deploy keys are genereated at runtime and added to private repo for checkout.
   * Deploy keys are exported to workspace before a build starts.
 - Add option to allow authenticated users to create jobs when using `DotCi Auth`.
 - Add environment variable `DOTCI_CHANGE_SET` with list of files in the current changeset as reported by github.
 - Docker builds now support `plugins` section in `.ci.yml`.
 - Option to configure additional tabs in Job configuration.

Bugfixes: 
  - Fixes https://github.com/groupon/DotCi/issues/69. Builds are now kicked off with default additional param values.
  - Fixes https://github.com/groupon/DotCi/issues/64.
  - Githook authenticates as `SYSTEM` user now.

##2.2.0(11/19/2014)
 - Changed serialization from XStream (XML) to Morphia (MongoDB/BSON)
    * Supports most classes that were previously serializable in XML
    * Does not currently support non-static inner classes
    * Includes [migration script] (./src/main/groovy/dotci_db_xml_morphia_migration.groovy) to assist transitioning from Mongo stored XML to BSON

##2.0.0(09/05/2014)
 - Introduced BuildType extension with two currently supported buildtypes
    * Install Packages
    * Docker Image

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
