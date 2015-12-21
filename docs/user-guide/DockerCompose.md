This build type uses [docker-compose](https://docs.docker.com/compose/) to execute the build.

## `.ci.yml` Sections

### `docker-compose-file` (Optional)

```yaml
docker-compose-file: "./jenkins/docker-compose.yml"
```
Specify an alternate compose file (default: docker-compose.yml)

### `before_run` (Optional)

```yaml
before_run: "./some_script && ./another_script"
```
Specify commands that should be run before the run commands. These commands will execute once. 


### `before_each` (Optional)

```yaml
before_each: "./some_script && ./another_script"
```
Specify commands that should be run before each run sub-command. 

### `after_each` (Optional)

```yaml
after_each: "./some_script && ./another_script"
```
Specify commands that should be run after each run sub-command. These will run on success and failure. 

### `run` (Required)

```yaml
run:
  test:
```
Runs a container defined by `test` in `docker-compose.yml` with its default `CMD`.


```yaml
run:
  test: 'npm test'
```
Runs a container defined by `test` in `docker-compose.yml` with the command `npm test`.

```yaml
run:
  test: 'rspec'
  cuke_test: 'cucumber'
  integration:
```
Parallel Run:

 - A container defined by `test` in `docker-compose.yml` with the command
  `rspec`.
 - A container defined by `cuke_test` in `docker-compose.yml` with the command
  `cucumber`.
 - A container defined by `integration` in `docker-compose.yml` with its default `CMD`.

### `skip` (Optional)

```yaml
skip: true
```
Skip build. 
This is useful when combined with templating. Eg, 

```yaml
#only build master or pull request.
<% if (DOTCI_BRANCH != 'master' && !DOTCI_PULL_REQUEST ) { %>
skip:
<% } %>
run:
  ci: 
```


### `plugins`
See [Plugins](Plugins)

### `notifications`
See [Notifications](Notifications)
