/*
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
 */
package com.groupon.jenkins.dynamic.build.execution;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.model.AbstractBuild;
import hudson.model.ParametersAction;
import hudson.tasks.BuildWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicBuild;

public class BuildEnvironment {

	private final DynamicBuild build;
	private final Launcher launcher;
	private final BuildListener listener;

	public BuildEnvironment(DynamicBuild build, Launcher launcher, BuildListener listener) {
		this.build = build;
		this.launcher = launcher;
		this.listener = listener;
	}

	private List<Environment> getBuildEnvironments() {
		return build.getEnvironments();
	}

	public boolean setupWrappers(List<BuildWrapper> wrappers) throws IOException, InterruptedException {
		for (BuildWrapper w : wrappers) {
			Environment e = w.setUp((AbstractBuild<?, ?>) build, launcher, listener);
			if (e == null) {
				return false;
			}
			List<Environment> buildEnvironments = getBuildEnvironments();
			buildEnvironments.add(e);
		}

		return true;

	}

	protected boolean tearDownBuildEnvironments(BuildListener listener) throws IOException, InterruptedException {
		boolean failed = false;
		List<Environment> buildEnvironments = getBuildEnvironments();
		for (int i = buildEnvironments.size() - 1; i >= 0; i--) {
			if (!buildEnvironments.get(i).tearDown(build, listener)) {
				failed = true;
			}
		}
		return failed;
	}

	public boolean initialize() throws IOException, InterruptedException {
		List<BuildWrapper> wrappers = new ArrayList<BuildWrapper>(getProject().getBuildWrappers().values());
		ParametersAction parameters = getAction(ParametersAction.class);
		if (parameters != null) {
			parameters.createBuildWrappers(build, wrappers);
		}
		return setupWrappers(wrappers);

	}

	private ParametersAction getAction(Class<ParametersAction> clazz) {
		return build.getAction(clazz);
	}

	private DynamicProject getProject() {
		return build.getParent();
	}

}
