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

package com.groupon.jenkins.buildtype.install_packages;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.buildtype.InvalidBuildConfigurationException;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.BuildConfiguration;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.BuildConfigurationCalculator;
import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import com.groupon.jenkins.buildtype.util.shell.ShellScriptRunner;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicSubBuild;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import com.groupon.jenkins.dynamic.build.execution.SubBuildRunner;
import com.groupon.jenkins.dynamic.build.execution.SubBuildScheduler;
import com.groupon.jenkins.dynamic.buildtype.BuildType;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.model.BuildListener;
import hudson.model.Result;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class InstallPackagesBuild extends BuildType implements SubBuildRunner{
    private static final Logger LOGGER = Logger.getLogger(InstallPackagesBuild.class.getName());
    private BuildConfiguration buildConfiguration;

    @Override
    public String getDescription() {
        return "Install Packages";
    }

    @Override
    public Result runBuild(DynamicBuild dynamicBuild, BuildExecutionContext buildExecutionContext, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        this.buildConfiguration = calculateBuildConfiguration(dynamicBuild, listener);
        if (buildConfiguration.isSkipped()) {
            dynamicBuild.skip();
            return Result.SUCCESS;
        }

        dynamicBuild.setAxisList(getAxisList(buildConfiguration));
       Result result ;
        if(buildConfiguration.isParallized()){
            result = runMultiConfigbuildRunner(dynamicBuild, buildConfiguration, listener, launcher);;
        }else{
             result = runSingleConfigBuild(dynamicBuild, new Combination(ImmutableMap.of("script", "main")),buildConfiguration,buildExecutionContext,listener,launcher) ;
        }
        runPlugins(dynamicBuild, buildConfiguration.getPlugins(), listener, launcher);
        runNotifiers(dynamicBuild,buildConfiguration,listener);
        return result;
    }


    private boolean runNotifiers(DynamicBuild build, BuildConfiguration buildConfiguration, BuildListener listener) {
        boolean result = true ;
        List<PostBuildNotifier> notifiers = buildConfiguration.getNotifiers();
        for (PostBuildNotifier notifier : notifiers) {
            result = result & notifier.perform(build, listener);
        }
        return result;
    }

    @Override
    public Result runSubBuild(Combination combination, BuildExecutionContext dynamicSubBuildExecution, BuildListener listener) throws IOException, InterruptedException {
        return runBuildCombination(combination,dynamicSubBuildExecution,listener);
    }

    private Result runMultiConfigbuildRunner(final DynamicBuild dynamicBuild, final BuildConfiguration buildConfiguration, final BuildListener listener, Launcher launcher)throws InterruptedException, IOException {
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

    private Result runSingleConfigBuild(DynamicBuild dynamicBuild, Combination combination, BuildConfiguration buildConfiguration, BuildExecutionContext buildExecutionContext, BuildListener listener, Launcher launcher) throws IOException, InterruptedException {
        return runBuildCombination(combination, buildExecutionContext, listener);
    }

    private void runPlugins(DynamicBuild dynamicBuild, List<DotCiPluginAdapter> plugins, BuildListener listener, Launcher launcher) {
        for(DotCiPluginAdapter plugin : plugins){
            plugin.perform(dynamicBuild, launcher, listener);
        }
    }

    private Result runBuildCombination(Combination combination,BuildExecutionContext buildExecutionContext, BuildListener listener) throws IOException, InterruptedException {
        ShellCommands mainBuildScript = buildConfiguration.toScript(combination);
        return new ShellScriptRunner(buildExecutionContext,listener).runScript(mainBuildScript);
    }




    private BuildConfiguration calculateBuildConfiguration(DynamicBuild build, BuildListener listener) throws IOException, InterruptedException, InvalidBuildConfigurationException {
        return new BuildConfigurationCalculator().calculateBuildConfiguration(build.getGithubRepoUrl(), build.getSha(), build.getEnvironmentWithChangeSet(listener));
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
