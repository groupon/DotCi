This build type uses [docker-compose](https://docs.docker.com/compose/) to execute the build.

## `.ci.yml` Sections

### `before`

```
before: "./some_script && ./another_script"
```
Specify any commands that should be run before building the image.

### `run`

```
run:
  test:
```
Runs a container defined by `test` in `docker-compose.yml` with its default `CMD`.


```
run:
  test: 'npm test'
```
Runs a container defined by `test` in `docker-compose.yml` with the command `npm test`.

```
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

### `plugins`
See [Plugins](Plugins)

### `notifications`
See [Notifications](Notifications)
