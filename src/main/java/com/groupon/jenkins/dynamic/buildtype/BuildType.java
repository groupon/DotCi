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
package com.groupon.jenkins.dynamic.buildtype;

import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import com.groupon.jenkins.dynamic.build.execution.WorkspaceFileExporter;
import hudson.matrix.Combination;
import hudson.model.BuildListener;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.tasks.Shell;
import java.io.IOException;

public abstract class BuildType {
//	BareMetal {
//		@Override
//		public Result runBuild(DbBackedBuild<?, ?> build, Combination combination, BuildExecutionContext buildContext, BuildListener listener) throws IOException, InterruptedException {
//			String mainBuildScript = buildContext.getBuildConfiguration().toScript(combination, this).toShellScript();
//			return runShellScript(buildContext, listener, mainBuildScript);
//		}
//	},
//	DockerImage {
//		@Override
//		public Result runBuild(DbBackedBuild<?, ?> build, Combination combination, BuildExecutionContext buildContext, BuildListener listener) throws IOException, InterruptedException {
//			String buildScriptForDocker = buildContext.getBuildConfiguration().toScript(combination, this).toShellScript();
//
//			String dockerBuildRunScript = buildContext.getBuildConfiguration().getBuildRunScriptForDockerImage(build.getBuildId(), build.getDotCiEnvVars(build.getJenkinsEnvVariables(listener)));
//			String dockerCleanupScript = buildContext.getBuildConfiguration().getDockerBuildRunCleanupScript(build.getBuildId());
//
//			Result checkoutResult = runShellScript(buildContext, listener, buildContext.getBuildConfiguration().getCheckoutScript());
//
//			Result buildScriptExportResult = checkoutResult.combine(createWorkspaceFile(buildContext, "dotci_build_script.sh", listener, buildScriptForDocker));
//			if (buildScriptExportResult.equals(Result.SUCCESS)) {
//				Result runResult = buildScriptExportResult.combine(runShellScript(buildContext, listener, dockerBuildRunScript));
//				runShellScript(buildContext, listener, dockerCleanupScript);
//				return runResult;
//			}
//			return buildScriptExportResult;
//		}
//	},
//	DockerLocal {
//		@Override
//		public Result runBuild(DbBackedBuild<?, ?> build, Combination combination, BuildExecutionContext buildContext, BuildListener listener) throws IOException, InterruptedException {
//			String buildScriptForDocker = "sh -c \"" + buildContext.getBuildConfiguration().toScript(combination, this).toShellCommand() + "\"";
//			String dockerBuildRunScript = buildContext.getBuildConfiguration().getBuildRunScriptForDockerLocal(build.getBuildId(), build.getDotCiEnvVars(build.getJenkinsEnvVariables(listener)), buildScriptForDocker);
//			String dockerCleanupScript = buildContext.getBuildConfiguration().getDockerBuildRunCleanupScript(build.getBuildId());
//			Result checkoutResult = runShellScript(buildContext, listener, buildContext.getBuildConfiguration().getCheckoutScript());
//			return checkoutResult.combine(runShellScript(buildContext, listener, dockerBuildRunScript)).combine(runShellScript(buildContext, listener, dockerCleanupScript));
//		}
//	};

	public Result createWorkspaceFile(BuildExecutionContext dynamicBuildRun, String fileName, BuildListener listener, String contents) throws IOException, InterruptedException {
		Result r = Result.FAILURE;
		try {
			if (dynamicBuildRun.performStep(new WorkspaceFileExporter(fileName, contents), listener)) {
				r = Result.SUCCESS;
			}
		} catch (InterruptedException e) {
			r = Executor.currentExecutor().abortResult();
			throw e;
		} finally {
			dynamicBuildRun.setResult(r);
		}
		return r;
	}

	public Result runShellScript(BuildExecutionContext dynamicBuildRun, BuildListener listener, String script) throws IOException, InterruptedException {
		Result r = Result.FAILURE;
		try {
			Shell execution = new Shell("#!/bin/bash -le \n" + script);
			if (dynamicBuildRun.performStep(execution, listener)) {
				r = Result.SUCCESS;
			}
		} catch (InterruptedException e) {
			r = Executor.currentExecutor().abortResult();
			throw e;
		} finally {
			dynamicBuildRun.setResult(r);
		}
		return r;
	}

	public abstract Result runBuild(DbBackedBuild<?, ?> build, Combination combination, BuildExecutionContext buildContext, BuildListener listener) throws IOException, InterruptedException;

    public static BuildType getBuildType(DynamicBuild dynamicBuild) {
        //		public BuildType getBuildType() throws IOException {
//			if (getBuildConfiguration().isDocker()) {
//				return BuildType.DockerImage;
//			}
//			if (new GithubRepositoryService(getGithubRepoUrl()).hasDockerFile(getSha())) {
//				return BuildType.DockerLocal;
//			}
//			return BuildType.BareMetal;
//		}
        return null;
    }
}
