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
import hudson.model.Executor;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.google.common.collect.Iterables;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.buildconfiguration.BuildConfiguration;
import com.groupon.jenkins.dynamic.buildconfiguration.InvalidDotCiYmlException;

public class DynamicBuildExection {

	private transient final DynamicBuild build;
	private final BuildEnvironment buildEnvironment;
	private final DynamicBuildRunner dynamicBuildRunner;

	private static DynamicBuildRunner getBuildRunner(DynamicBuild build, BuildExecutionContext dynamicBuildRun, BuildType buildType, DotCiPluginRunner dotCiPluginRunner) {
		return build.getBuildConfiguration().isParallized() ? new DynamicMultiConfigBuildRunner(build, buildType, dotCiPluginRunner) : new DynamicSingleConfigBuildRunner(build, dynamicBuildRun, buildType, dotCiPluginRunner, Iterables.getOnlyElement(build.getAxisList()));
	}

	public DynamicBuildExection(DynamicBuild build, BuildEnvironment buildEnvironment, BuildExecutionContext dynamicBuildRun, DotCiPluginRunner dotCiPluginRunner, BuildType buildType) {
		this(build, buildEnvironment, getBuildRunner(build, dynamicBuildRun, buildType, dotCiPluginRunner));
	}

	public DynamicBuildExection(DynamicBuild build, BuildEnvironment buildEnvironment, DynamicBuildRunner dynamicBuildRunner) {
		this.build = build;
		this.dynamicBuildRunner = dynamicBuildRunner;
		this.buildEnvironment = buildEnvironment;
	}

	public Result doRun(BuildListener listener) throws IOException, InterruptedException {
		try {
			if (!buildEnvironment.initialize()) {
				return Result.FAILURE;
			}

			if (getBuildConfiguration().isSkipped()) {
				build.skip();
				return Result.SUCCESS;
			}
			build.setDescription(build.getCause().getBuildDescription());
			return dynamicBuildRunner.runBuild(listener);
		} catch (InterruptedException e) {
			Executor x = Executor.currentExecutor();
			x.recordCauseOfInterruption(build, listener);
			return x.abortResult();
		} catch (InvalidDotCiYmlException e) {
			throw e;
		} catch (Exception e) {
			PrintStream logger = listener.getLogger();
			logger.println(e.getMessage());
			logger.println(ExceptionUtils.getStackTrace(e));
			Executor x = Executor.currentExecutor();
			x.recordCauseOfInterruption(build, listener);
			x.doStop();
			return Result.FAILURE;
		} finally {
			if (buildEnvironment.tearDownBuildEnvironments(listener)) {
				return Result.FAILURE;
			}
		}
	}

	private BuildConfiguration getBuildConfiguration() {
		return build.getBuildConfiguration();
	}

}
