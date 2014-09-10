/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.groupon.jenkins.buildtype.dockerfile;

import com.groupon.jenkins.buildtype.InvalidBuildConfigurationException;
import com.groupon.jenkins.buildtype.docker.CheckoutCommands;
import com.groupon.jenkins.buildtype.docker.DockerBuild;
import com.groupon.jenkins.buildtype.docker.DockerBuildConfiguration;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import com.groupon.jenkins.dynamic.build.execution.WorkspaceFileExporter;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.Combination;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

@Extension
public class DockerfileBuild extends DockerBuild{

    @Override
    public String getDescription() {
        return "Dockerfile Build";
    }

    @Override
    public Result runBuild(DynamicBuild build, BuildExecutionContext buildExecutionContext, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        exportDockerFileIntoWorkspace(build,launcher,listener);
        return super.runBuild(build, buildExecutionContext, launcher, listener);
    }

    private void exportDockerFileIntoWorkspace(DynamicBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        new WorkspaceFileExporter("Dockerfile",fetchDockerFile(build)).perform((AbstractBuild)build,launcher,listener);
    }

    private String fetchDockerFile(DynamicBuild build) throws IOException {
        try{
            return build.getGithubRepositoryService().getGHFile("Dockerfile", build.getSha()).getContent();
        } catch (FileNotFoundException _){
            throw new InvalidBuildConfigurationException("No Dockerfile found.");
        }
    }

    @Override
    public Result runSubBuild(Combination combination, BuildExecutionContext buildExecutionContext, BuildListener listener) throws IOException, InterruptedException {
        return super.runSubBuild(combination, buildExecutionContext, listener);
    }

    @Override
    public DockerBuildConfiguration getBuildConfiguration(Map config, String buildId, EnvVars buildEnvironment) {
        return new DockerfileBuildConfiguration(config,buildId, CheckoutCommands.get(buildEnvironment));
    }
}
