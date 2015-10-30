DotCi defines three new [extension points]( https://wiki.jenkins-ci.org/display/JENKINS/Extension+points)

#### Add **new build type**, this would be populated in the build type dropdown.
Eg:

```java

@Extension
public class DockerImageBuild extends BuildType implements SubBuildRunner {
   @Override
    public String getDescription() {
        return "Docker Build";
    }

    @Override
    public Result runBuild(DynamicBuild build, BuildExecutionContext buildExecutionContext, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    }
}
```

#### Add **new notifications** is done by extending `PostBuildNotifier`

Eg: Adding a hipchat notifier in notifications section of `.ci.yml`

```java
  @Extension
public class HipchatNotifier extends PostBuildNotifier {
	public HipchatNotifier() {
		super("hipchat");
	}
	@Override
	public boolean notify(DynamicBuild build, BuildListener listener) {
	  //notify hipchat room
	}
```


#### Adding a **new plugin** for use through plugins section of `.ci.yml` is done by extending `DotCiPluginAdapter`

Eg: Adding cobertura to plugins section

```java
@Extension
public class CoberturaPluginAdapter extends DotCiPluginAdapter {

	public CoberturaPluginAdapter() {
		super("cobertura", "target/site/cobertura/coverage.xml");
	}

	@Override
	public boolean perform(DynamicBuild dynamicBuild, Launcher launcher, BuildListener listener) {
		CoberturaPublisher publisher = new CoberturaPublisher(pluginInputFiles, false, false, false, false, false, false, false, null, 0);

		try {
			return publisher.perform(((AbstractBuild) dynamicBuild), launcher, listener);
		} catch (Exception e) {
			e.printStackTrace(listener.getLogger());
			return false;
		}
	}

}
```
