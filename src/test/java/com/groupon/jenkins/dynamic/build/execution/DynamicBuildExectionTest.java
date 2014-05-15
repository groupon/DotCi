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

import hudson.model.Result;

import java.io.IOException;

import org.junit.Test;

import com.groupon.jenkins.dynamic.build.DynamicBuild;

import static com.groupon.jenkins.testhelpers.BuildConfigurationFactory.buildConfiguration;
import static com.groupon.jenkins.testhelpers.DynamicBuildFactory.newBuild;
import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DynamicBuildExectionTest {

	@Test
	public void should_fail_build_if_build_environment_errors_on_intialization() throws IOException, InterruptedException {
		BuildEnvironment buildEnvironment = mock(BuildEnvironment.class);
		when(buildEnvironment.initialize()).thenReturn(false);
		DynamicBuildExection buildExecution = new DynamicBuildExection(newBuild().get(), buildEnvironment, null);
		assertEquals(Result.FAILURE, buildExecution.doRun(null));
	}

	@Test
	public void should_skip_build_if_skip_is_specified() throws IOException, InterruptedException {
		BuildEnvironment buildEnvironment = mock(BuildEnvironment.class);
		when(buildEnvironment.initialize()).thenReturn(true);

		DynamicBuild build = newBuild().buildConfiguration(buildConfiguration().skipped().get()).get();

		DynamicBuildExection buildExecution = new DynamicBuildExection(build, buildEnvironment, null);
		buildExecution.doRun(null);
		verify(build).skip();
	}

	@Test
	public void should_run_build() throws IOException, InterruptedException {
		BuildEnvironment buildEnvironment = mock(BuildEnvironment.class);
		when(buildEnvironment.initialize()).thenReturn(true);

		DynamicBuild build = newBuild().buildConfiguration(buildConfiguration().get()).get();

		DynamicBuildRunner runner = mock(DynamicBuildRunner.class);
		DynamicBuildExection buildExecution = new DynamicBuildExection(build, buildEnvironment, runner);
		buildExecution.doRun(null);
		verify(runner).runBuild(null);
	}
}
