# Non-Docker Slave

A slave that can build for non-docker build types, i.e. [Install
Packages](../usage/ci_yml/build_types/InstallPackages.md) must be able
to install the various languages specified by the `environment` section.
DotCi will call a bash function `install_packages` with the requested
languages and packages to install. It is up to the slave to implement
this function.

For example a `.ci.yml` with:
```yaml
environment:
  language: ruby
  language_version: 1.9.3
packages:
  - libxml2-dev
```
Would call `install_packages ruby-1.9.3`
**FIXME: How does packages get passed in?**

A sample implementation with a slave that had [rvm](https://rvm.io/)
installed might be:
```bash
function install_packages(){
  gemset=`echo ${PWD##*/} | tr ',' '_'`
  set +e
  if [[ $1 =~ "ree" ]]; then
    rvm use `echo $1 | sed -e "s/^ruby-//g" -e "s/_/-/g"`@$gemset --create
  elif [[ $1 =~ "jruby" ]]; then
    rvm use `echo $1 | sed -e "s/^ruby-//g" -e "s/_/-/g"`@$gemset --create
  else
    rvm use `echo $1 | sed -e "s/_/-/g"`@$gemset --create
  fi
  rvm gemset empty --force
  set -e
}
```
