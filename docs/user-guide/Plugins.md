
### Jenkins  Plugins
 Any [Builder](https://wiki.jenkins-ci.org/display/JENKINS/Extension+points#Extensionpoints-hudson.tasks.Builder) and 
  [Publisher](http://javadoc.jenkins-ci.org/?hudson/tasks/Publisher.html) can be specified with ClassName and setter options

eg: 

```yaml
    plugins:
      - CheckStylePublisher: # https://github.com/jenkinsci/checkstyle-plugin/blob/master/src/main/java/hudson/plugins/checkstyle/CheckStylePublisher.java
           pattern: 'checkstyle/*.xml' #https://github.com/jenkinsci/checkstyle-plugin/blob/master/src/main/java/hudson/plugins/checkstyle/CheckStylePublisher.java#L65
```
###  Built-In Plugins
Plugins that are bundled with DotCi.
### `downstream_job`
```yaml
plugins:
  - downstream_job:
      on_result: <SUCCESS|UNSTABLE|FAILURE|NOT_BUILT|ABORTED> # From hudson.model.Result static types
      groupon/DotCi: # Repo
        k1: 'v1' #param k1
        k2: 'v2' #param k2
```
This triggers the job for the repo `groupon/DotCi` when the current job's
result matches the value of `on_result`, which comes from
[`hudson.model.Result`](https://github.com/kohsuke/hudson/blob/7a64e030a38561c98954c4c51c4438c97469dfd6/core/src/main/java/hudson/model/Result.java).
The hash that is the key `groupon/DotCi`'s value are passed into the downstream job as its parameterized values ( these don't necessarily have to be predefined in the target job).


##  Starter-Pack Plugins

There are optional set of plugins that are available if you install [DotCi-Plugins-Starter-Pack](https://github.com/groupon/DotCi-Plugins-Starter-Pack) from update center.


### `review_line_comments`
```yaml
plugins:
  - review_line_comments
```
Collects output from [Analysis Collector Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Analysis+Collector+Plugin) and Cobertura Plugin and converts them into line comments on github Pull Requests.

