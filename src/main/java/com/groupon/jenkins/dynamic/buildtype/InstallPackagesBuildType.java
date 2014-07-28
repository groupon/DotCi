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

package com.groupon.jenkins.dynamic.buildtype;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.execution.BuildEnvironment;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import com.groupon.jenkins.dynamic.build.execution.DotCiPluginRunner;
import com.groupon.jenkins.dynamic.build.execution.DynamicBuildExection;
import com.groupon.jenkins.dynamic.buildconfiguration.BuildConfiguration;
import com.groupon.jenkins.dynamic.buildconfiguration.BuildConfigurationCalculator;
import com.groupon.jenkins.dynamic.buildconfiguration.InvalidDotCiYmlException;
import hudson.Launcher;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.model.BuildListener;
import hudson.model.Result;
import java.io.IOException;

public class InstallPackagesBuildType extends BuildType {
    private DynamicBuild dynamicBuild;

    public InstallPackagesBuildType(DynamicBuild dynamicBuild) {
        this.dynamicBuild = dynamicBuild;
    }
//    @Override
//    public Result runBuild(DynamicBuild build, Combination combination, BuildExecutionContext buildContext, BuildListener listener) throws IOException, InterruptedException {
//        String mainBuildScript = calculateBuildConfiguration(build,listener).toScript(combination).toShellScript();
//        return runShellScript(buildContext, listener, mainBuildScript);
//    }

    @Override
    public boolean isParallized() {
        return false;
    }

    @Override
    public AxisList getAxisList(DynamicBuild build) {
//        BuildConfiguration buildConfiguration = null; // calculateBuildConfiguration(build,null);
//        if (buildConfiguration.isMultiLanguageVersions() && buildConfiguration.isMultiScript()) {
//            return new AxisList(new Axis("language_version", buildConfiguration.getLanguageVersions()), new Axis("script", buildConfiguration.getScriptKeys()));
//        }
//        if (buildConfiguration.isMultiLanguageVersions()) {
//            return new AxisList(new Axis("language_version", buildConfiguration.getLanguageVersions()));
//        }
//        if (buildConfiguration.isMultiScript()) {
//            return new AxisList(new Axis("script", buildConfiguration.getScriptKeys()));
//        }
        return new AxisList(new Axis("script", "main"));
    }

    @Override
    public Result runBuild(BuildExecutionContext dynamicRunExecution, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        //	DynamicBuild.this.setBuildConfiguration(calculateBuildConfiguration(listener));
        BuildEnvironment buildEnvironment = new BuildEnvironment(dynamicBuild, launcher, listener);
        DotCiPluginRunner dotCiPluginRunner = new DotCiPluginRunner(dynamicBuild, launcher);
        DynamicBuildExection dynamicBuildExecution = new DynamicBuildExection(dynamicBuild, buildEnvironment, this, dotCiPluginRunner);
//
        BuildConfiguration buildConfiguration = calculateBuildConfiguration(dynamicBuild, listener);
        return Result.SUCCESS;
    }

    private BuildConfiguration calculateBuildConfiguration(DynamicBuild build, BuildListener listener) throws IOException, InterruptedException, InvalidDotCiYmlException {
        return new BuildConfigurationCalculator().calculateBuildConfiguration(build.getGithubRepoUrl(), build.getSha(), build.getEnvironment(listener));
    }
}
