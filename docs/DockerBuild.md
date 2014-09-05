Docker builds expect a  `.ci.yml` in the root of the repository in the following format.

```yaml
image: ubuntu #name of image to run the build against
run_params: "-e PASSWORD=moew"  
command:
  - mvn setup
  - mvn test
links: #links to parent image using docker links
  - image: redis
    name: content-uat4 #optional; defaults to image name
    run_params: "-v `pwd`:/content" #optional run params
    links: # Links to parent container; links can be nested indefinitely
      - image: mysql

```

### Running builds in parallel

`command` key in `.ci.yml` can be a map
```yaml
command:
    unit: mvn unit
    integration: mvn integration
```
