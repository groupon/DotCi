# Templating .ci.yml

## Environment Variables
[Environment Variables](EnvironmentVariables.md)

## Groovy templating

`.ci.yml` acts as a [groovy
template](http://groovy.codehaus.org/Groovy+Templates) which is run
through a groovy preprocessor before build starts.

### Examples
Send extra notification to yourself for a build you started:
```yaml
notifications:
  <% if (DOTCI_PUSHER == 'joe') { %>
  - sms: 1234344453
  <% } %>
```

Run certain commands after tests when `DOTCI_BRANCH` is `production`:
```yaml
build:
   run: rake spec
   #run integration tests only on production branch
   <% if (DOTCI_BRANCH == 'production') { %>
   after: rake integration
   <%} %>
```

Notify hipchat room `DevOps` when `DOTCI_BRANCH` is `master`:
```yaml
notifications:
  <% if (DOTCI_BRANCH == 'master') { %>
  - hipchat: 'DevOps'
  <%}%>
```

Pass `DOTCI_BRANCH` as a parameter to webhook:
```yaml
plugins:
  - webhook:
      url: http://example.com/hook
      params:
        branch: ${DOTCI_BRANCH}
```

Artifact files when `DOTCI_BRANCH` is `master`:
```yaml
build:
  <% if( DOTCI_BRANCH != 'master') {%>
  - artifacts: 'packages/**/*.war'
  <%}%>
```
