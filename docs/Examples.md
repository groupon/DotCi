##.ci.yml examples


###Parallelization
```yaml
build:
  run:
    unit: rake spec
    integration: rake integration
```
![dotci setup](screenshots/script-parallized.png)
###Build Environment Configuration

#### non-docker ruby build parallelized across two language versions
```yaml
  environment:
     language: ruby
     language_versions:
          - 1.8.7
          - 1.9.3
```

#### docker
```yaml
environment:
   image: centos
   services:
      - google/mysql
      - google/redis
```

## Build templating

```yaml
 build:
    run: rake spec
    #run integration tests only on production branch
    <% if(DOTCI_BRANCH == 'production') %>
    after: rake integration
    <%end %>
 notifications:
   <% if(DOTCI_BRANCH == 'master') %>
   - hipchat: 'DevOps'
   <%end %>
```

## Plugin Configuration
 ```yaml
  plugins:
    <% if(DOTCI_BRANCH == 'master') %>
    - artifacts: 'packages/**/*.war'
    <%end%>
    - checkstyle
    - webhook:
        url: http://example.com/hook
        params:
          branch: ${DOTCI_BRANCH}
 ```

##Build skipping
 ```yaml
   build:
     #only build master
     <% if(DOTCI_BRANCH != 'master') %>
     - artifacts: 'packages/**/*.war'
     <%end%>
 ```
