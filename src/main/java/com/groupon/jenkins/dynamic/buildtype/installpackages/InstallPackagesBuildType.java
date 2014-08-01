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

package com.groupon.jenkins.dynamic.buildtype.installpackages;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicBuildLayouter;
import com.groupon.jenkins.dynamic.build.DynamicSubBuild;
import com.groupon.jenkins.dynamic.build.execution.BuildEnvironment;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import com.groupon.jenkins.dynamic.build.execution.DotCiPluginRunner;
import com.groupon.jenkins.dynamic.build.execution.SubBuildScheduler;
import com.groupon.jenkins.dynamic.buildtype.installpackages.buildconfiguration.BuildConfiguration;
import com.groupon.jenkins.dynamic.buildtype.installpackages.buildconfiguration.BuildConfigurationCalculator;
import com.groupon.jenkins.dynamic.buildtype.installpackages.buildconfiguration.InvalidDotCiYmlException;
import com.groupon.jenkins.dynamic.buildtype.installpackages.buildconfiguration.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.dynamic.buildtype.BuildType;
import hudson.Launcher;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.model.BuildListener;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.tasks.Shell;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.exception.ExceptionUtils;

public class InstallPackagesBuildType extends BuildType {
    private DynamicBuild dynamicBuild;
    private static final Logger LOGGER = Logger.getLogger(InstallPackagesBuildType.class.getName());
    private BuildConfiguration buildConfiguration;


    public InstallPackagesBuildType(DynamicBuild dynamicBuild) {
        this.dynamicBuild = dynamicBuild;
    }


    @Override
    public Result runBuild(BuildExecutionContext buildExecutionContext, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        BuildEnvironment buildEnvironment = new BuildEnvironment(dynamicBuild, launcher, listener);
        DotCiPluginRunner dotCiPluginRunner = new DotCiPluginRunner(dynamicBuild, launcher);
        this.buildConfiguration = calculateBuildConfiguration(dynamicBuild, listener);
        try {
            if (!buildEnvironment.initialize()) {
                return Result.FAILURE;
            }

            if (buildConfiguration.isSkipped()) {
                dynamicBuild.skip();
                return Result.SUCCESS;
            }

            setLayouter(buildConfiguration);
            dynamicBuild.setDescription(dynamicBuild.getCause().getBuildDescription());
            if(buildConfiguration.isParallized()){
                return runMultiConfigbuildRunner(buildConfiguration,buildExecutionContext,listener, dotCiPluginRunner) ;
            }else{
                return runSingleConfigBuild(new Combination(ImmutableMap.of("script", "main")),buildConfiguration,buildExecutionContext,listener ,dotCiPluginRunner) ;
            }

        } catch (InterruptedException e) {
            Executor x = Executor.currentExecutor();
            x.recordCauseOfInterruption(dynamicBuild, listener);
            return x.abortResult();
        } catch (InvalidDotCiYmlException e) {
            throw e;
        } catch (Exception e) {
            PrintStream logger = listener.getLogger();
            logger.println(e.getMessage());
            logger.println(ExceptionUtils.getStackTrace(e));
            Executor x = Executor.currentExecutor();
            x.recordCauseOfInterruption(dynamicBuild, listener);
            x.doStop();
            return Result.FAILURE;
        } finally {
            if (buildEnvironment.tearDownBuildEnvironments(listener)) {
                return Result.FAILURE;
            }
        }
    }

    private Result runMultiConfigbuildRunner(final BuildConfiguration buildConfiguration, BuildExecutionContext buildExecutionContext, final BuildListener listener, DotCiPluginRunner dotCiPluginRunner)throws InterruptedException, IOException {
        SubBuildScheduler subBuildScheduler = new SubBuildScheduler(dynamicBuild, this, new SubBuildScheduler.SubBuildFinishListener() {
            @Override
            public void runFinished(DynamicSubBuild subBuild) throws IOException {
                for (DotCiPluginAdapter plugin : buildConfiguration.getPlugins()) {
                    plugin.runFinished(subBuild, dynamicBuild, listener);
                }
            }
        });

        try {
            Iterable<Combination> axisList = getAxisList(buildConfiguration).list();
            Result combinedResult = subBuildScheduler.runSubBuilds(getMainRunCombinations(axisList), listener);
            if (combinedResult.equals(Result.SUCCESS) && !Iterables.isEmpty(getPostBuildCombination(axisList))) {
                Result runSubBuildResults = subBuildScheduler.runSubBuilds(getPostBuildCombination(axisList), listener);
                combinedResult = combinedResult.combine(runSubBuildResults);
            }
            dynamicBuild.setResult(combinedResult);
            dotCiPluginRunner.runPlugins(listener);
            return combinedResult;
        } finally {
            try {
                subBuildScheduler.cancelSubBuilds(listener.getLogger());
            } catch (Exception e) {
                // There is nothing much we can do at this point
                LOGGER.log(Level.SEVERE, "Failed to cancel subbuilds", e);
            }
        }
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

    private Result runSingleConfigBuild(Combination combination, BuildConfiguration buildConfiguration, BuildExecutionContext buildExecutionContext, BuildListener listener, DotCiPluginRunner dotCiPluginRunner) throws IOException, InterruptedException {
        String mainBuildScript = buildConfiguration.toScript(combination).toShellScript();
        Result result = runShellScript(buildExecutionContext, listener, mainBuildScript);
        dotCiPluginRunner.runPlugins(listener);
        return result;
    }


    @Override
    public Result runSubBuild(Combination combination, BuildExecutionContext dynamicSubBuildExecution, BuildListener listener) throws IOException, InterruptedException {
      return runSingleConfigBuild(combination, buildConfiguration,dynamicSubBuildExecution,listener,DotCiPluginRunner.NOOP);
    }

    private BuildConfiguration calculateBuildConfiguration(DynamicBuild build, BuildListener listener) throws IOException, InterruptedException, InvalidDotCiYmlException {
        return new BuildConfigurationCalculator().calculateBuildConfiguration(build.getGithubRepoUrl(), build.getSha(), build.getEnvironment(listener));
    }

    public void setLayouter(BuildConfiguration buildConfiguration) {
        AxisList axisList = getAxisList(buildConfiguration);
        DynamicBuildLayouter dynamicBuildLayouter = new DynamicBuildLayouter(axisList, dynamicBuild);
        dynamicBuild.setDyanamicBuildLayouter(dynamicBuildLayouter);
    }

    private AxisList getAxisList(BuildConfiguration buildConfiguration) {
        AxisList  axisList = new AxisList(new Axis("script", "main"));
        if (buildConfiguration.isMultiLanguageVersions() && buildConfiguration.isMultiScript()) {
            axisList = new AxisList(new Axis("language_version", buildConfiguration.getLanguageVersions()), new Axis("script", buildConfiguration.getScriptKeys()));
        }
        else if (buildConfiguration.isMultiLanguageVersions()) {
            axisList = new AxisList(new Axis("language_version", buildConfiguration.getLanguageVersions()));
        }
        else if (buildConfiguration.isMultiScript()) {
            axisList = new AxisList(new Axis("script", buildConfiguration.getScriptKeys()));
        }
        return axisList;
    }

    public List<Combination> getPostBuildCombination(Iterable<Combination> axisList) {
        for (Combination combination : axisList) {
            if (isPostBuild(combination)) {
                return Arrays.asList(combination);
            }
        }
        return Collections.emptyList();
    }

    private boolean isPostBuild(Combination combination) {
        return "post_build".equals(combination.get("script"));
    }

    public Iterable<Combination> getMainRunCombinations(Iterable<Combination> axisList) {
        return Iterables.filter(axisList, new Predicate<Combination>() {
            @Override
            public boolean apply(Combination combination) {
                return !isPostBuild(combination);
            }
        });
    }

}
