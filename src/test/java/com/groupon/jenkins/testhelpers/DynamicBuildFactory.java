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
package com.groupon.jenkins.testhelpers;

import hudson.model.Result;
import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Cause.UserIdCause;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.mockito.stubbing.OngoingStubbing;

import com.google.common.collect.Lists;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicSubBuild;
import com.groupon.jenkins.dynamic.build.DynamicSubProject;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.BuildConfiguration;

import static com.groupon.jenkins.testhelpers.TestHelpers.map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DynamicBuildFactory {

	private final DynamicBuild build;

	public DynamicBuildFactory() {
		this.build = mock(DynamicBuild.class);
		when(build.getNumber()).thenReturn(new Random().nextInt());
		when(build.getSha()).thenReturn("sha-1234");
		when(build.getFullUrl()).thenReturn("http://absolute.url/meow");
		when(build.getCause()).thenReturn(mock(BuildCause.class));
	}

	public static DynamicBuildFactory newBuild() {
		return new DynamicBuildFactory();
	}

	public DynamicBuild get() {
		return build;
	}

	public DynamicBuildFactory skipped() {
		when(build.isSkipped()).thenReturn(true);
		return this;
	}

	public DynamicBuildFactory building() {
		when(build.isBuilding()).thenReturn(true);
		return this;
	}

	public DynamicBuildFactory manualStart(String userId, String branch) {
		UserIdCause userIdCause = mock(UserIdCause.class);
		when(userIdCause.getUserId()).thenReturn(userId);
		when(build.getCause(UserIdCause.class)).thenReturn(userIdCause);
		addBranchParam(branch);
		return this;
	}

	private OngoingStubbing<Map<String, String>> addBranchParam(String branch) {
		return when(build.getEnvVars()).thenReturn(map("BRANCH", branch));
	}

	public DynamicBuildFactory withSubBuilds(DynamicSubBuild... subBuilds) {
		DynamicProject dynamicProject = mock(DynamicProject.class);
		List<DynamicSubProject> subProjects = new ArrayList<DynamicSubProject>(subBuilds.length);
		for (DynamicSubBuild subBuild : subBuilds) {
			DynamicSubProject subProject = mock(DynamicSubProject.class);
			when(subProject.getBuildByNumber(build.getNumber())).thenReturn(subBuild);
			subProjects.add(subProject);
		}
		when(dynamicProject.getItems()).thenReturn(subProjects);
		when(build.getParent()).thenReturn(dynamicProject);
		return this;
	}

	public DynamicBuildFactory fail() {
		when(build.getResult()).thenReturn(Result.FAILURE);
		return this;
	}

	public DynamicBuildFactory recovery() {
		DynamicBuild failedPrevBuild = newBuild().fail().get();
		when(build.getPreviousFinishedBuildOfSameBranch(null)).thenReturn(failedPrevBuild);
		return this;
	}

	public DynamicBuildFactory success() {
		when(build.getResult()).thenReturn(Result.SUCCESS);
		return this;
	}

	public DynamicBuildFactory notRecovery() {
		DynamicBuild successPrevBuild = newBuild().success().get();
		when(build.getPreviousFinishedBuildOfSameBranch(null)).thenReturn(successPrevBuild);
		return this;
	}

	public DynamicBuildFactory unstable() {
		when(build.getResult()).thenReturn(Result.UNSTABLE);
		return this;
	}

	public DynamicBuildFactory upstreamStart(String branch) {
		Cause cause = mock(UpstreamCause.class);
		when(build.getCauses()).thenReturn(Lists.newArrayList(cause));
		addBranchParam(branch);
		return this;
	}

	public DynamicBuildFactory buildConfiguration(BuildConfiguration buildConfiguration) {
	//	when(build.getBuildConfiguration()).thenReturn(buildConfiguration);
		return this;
	}

}
