Docker builds in DotCi require Docker Compose  https://docs.docker.com/compose/

To use this build type, select the "Docker Compose" build type in your project's configure page, as documented in the [Usage](Usage.md#override-default-build-type) section.

.ci.yml has three main sections

##Run
*  Run a container defined with the name 'test' in docker-compose.yml with the default `CMD` as defined in the `Dockerfile`

   ```
    run:
     test:
    ```
*  Run a container from docker-compose.yml with command defined in .ci.yml

   ```
    run:
      test: 'npm test'
    ```
* Run multiple services/tests in parallel

     ```
    run:
      test: 'rspec'
      cuke_test: 'cucumber'
      integration:
    ```
   This will launch 3 test services in parallel

# Plugins
  Run plugins defined in .ci.yml

# Notifications
  Run notifications defined in .ci.yml

