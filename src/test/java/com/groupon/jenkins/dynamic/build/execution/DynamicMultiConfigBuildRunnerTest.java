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

import hudson.model.BuildListener;
import hudson.model.Result;

import java.io.IOException;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.groupon.jenkins.dynamic.build.DynamicSubProject;
import com.groupon.jenkins.dynamic.build.DynamicBuild;

import static com.groupon.jenkins.testhelpers.DynamicBuildFactory.newBuild;
import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DynamicMultiConfigBuildRunnerTest {

	@Test
	public void should_run_parallized_builds() throws InterruptedException, IOException {
		SubBuildScheduler subBuildScheduler = mock(SubBuildScheduler.class);
		BuildListener listener = mock(BuildListener.class);
		DynamicBuild build = newBuild().get();
		DynamicMultiConfigBuildRunner dynamicMultiConfigBuildRunner = new DynamicMultiConfigBuildRunner(build, subBuildScheduler, DotCiPluginRunner.NOOP, mock(BuildType.class));
		when(subBuildScheduler.runSubBuilds(build.getRunSubProjects(), listener)).thenReturn(Result.SUCCESS);
		Result runResult = dynamicMultiConfigBuildRunner.runBuild(listener);
		assertEquals(Result.SUCCESS, runResult);
	}

	@Test
	public void should_run_post_build_builds_if_parallized_builds_is_successful() throws InterruptedException, IOException {
		SubBuildScheduler subBuildScheduler = mock(SubBuildScheduler.class);
		BuildListener listener = mock(BuildListener.class);
		DynamicBuild build = newBuild().get();
		Iterable<DynamicSubProject> afterProjects = Lists.newArrayList(mock(DynamicSubProject.class));
		when(build.getPostBuildSubProjects()).thenReturn(afterProjects);

		DynamicMultiConfigBuildRunner dynamicMultiConfigBuildRunner = new DynamicMultiConfigBuildRunner(build, subBuildScheduler, DotCiPluginRunner.NOOP, mock(BuildType.class));
		when(subBuildScheduler.runSubBuilds(build.getRunSubProjects(), listener)).thenReturn(Result.SUCCESS);
		when(subBuildScheduler.runSubBuilds(afterProjects, listener)).thenReturn(Result.FAILURE);
		Result runResult = dynamicMultiConfigBuildRunner.runBuild(listener);
		assertEquals(Result.FAILURE, runResult);
	}

	@Test
	public void should_run_plugins_after_post_build() throws InterruptedException, IOException {
		SubBuildScheduler subBuildScheduler = mock(SubBuildScheduler.class);
		BuildListener listener = mock(BuildListener.class);
		DynamicBuild build = newBuild().get();
		Iterable<DynamicSubProject> afterProjects = Lists.newArrayList(mock(DynamicSubProject.class));
		when(build.getPostBuildSubProjects()).thenReturn(afterProjects);
		DotCiPluginRunner pluginRunner = mock(DotCiPluginRunner.class);

		DynamicMultiConfigBuildRunner dynamicMultiConfigBuildRunner = new DynamicMultiConfigBuildRunner(build, subBuildScheduler, pluginRunner, mock(BuildType.class));
		when(subBuildScheduler.runSubBuilds(build.getRunSubProjects(), listener)).thenReturn(Result.SUCCESS);
		when(subBuildScheduler.runSubBuilds(afterProjects, listener)).thenReturn(Result.FAILURE);
		dynamicMultiConfigBuildRunner.runBuild(listener);

		verify(pluginRunner).runPlugins(listener);
	}
}
