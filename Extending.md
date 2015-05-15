DotCi defines three new [extension points] ( https://wiki.jenkins-ci.org/display/JENKINS/Extension+points)

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
###LICENSE

The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.  
