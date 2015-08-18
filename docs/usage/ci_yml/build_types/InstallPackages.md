# Install Packages

This build type automatically uses a pre-defined `.ci.yml` template based on the
language that is autodetected. A repo provided `.ci.yml` can be used
to _override_ settings in various sections. The resultant build will be a
merge between the pre-defined template and any overriden settings.

The `.ci.yml` file is divided into four major sections:
* [Environment Section](#environment-section)
* [Build Section](#build-section)
* [Plugins Section](../Plugins.md)
* [Notifications Section](../Notifications.md)


### `environment` Section
```yaml
environment:
  # These vars are exported in shell before build starts
  vars:
     BUNDLE_WITHOUT: production:development

  # Single value defining the target language
  # Acceptable values are:
  #  clojure, coffeescript, go, java, javascript, ruby
  language: ruby

  # Single version or list of versions to run build within;
  # multiple values results in a matrix build
  language_versions:
    - ree-1.8.7
    - mri-1.8.7

  # Extra arguments that passed into install_packages call
  packages:
    - memcached-1.4.5
```

### `build` Section
```yaml
build:
  # Skips the build if true
  skip: true

  # Single command or list of commands to run before tests
  # (run serially on each matrix job)
  before:
    - gem install -y rubygems-update
    - update_rubygems

  # Single command or list of commands to get diagnostics
  info:
    - bundle --version

  # Single command to run tests
  run: rake test
  # Multiple commands to run tests
  # (run serially on each matrix job)
  run:
    - rake test
    - rake cucumber
  # Parallel tests
  # (run each hash map entry in parallel for a given matrix configuration)
  run:
    unit:        bundle exec rake spec:units
    integration: bundle exec rake spec:integration
    # Runs each command serially
    acceptance:
      - bundle exec rake cucumber
      - bundle exec rake cucumber:javascript

  # Single command or list of commands to run after tests
  # (run serially on each matrix job)
  after: cap deploy staging
```

### `plugins` Section
See [Plugins](../Plugins.md)

### `notifications` Section
See [Notifications](../Notifications.md)


## Examples

### Parallelization Output
![dotci setup](screenshots/script-parallized.png)
