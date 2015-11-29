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

package com.groupon.jenkins.buildtype.dockercompose;

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.buildtype.InvalidBuildConfigurationException;
import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import com.groupon.jenkins.buildtype.util.shell.ShellScriptRunner;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicSubBuild;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import com.groupon.jenkins.dynamic.build.execution.SubBuildRunner;
import com.groupon.jenkins.dynamic.build.execution.SubBuildScheduler;
import com.groupon.jenkins.dynamic.buildtype.BuildType;
import com.groupon.jenkins.git.GitUrl;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import com.groupon.jenkins.util.GroovyYamlTemplateProcessor;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.Combination;
import hudson.model.BuildListener;
import hudson.model.Result;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Extension
public class DockerComposeBuild extends BuildType implements SubBuildRunner {
    private BuildConfiguration buildConfiguration;

    @Override
    public String getDescription() {
        return "Docker Compose Build";
    }

    @Override
    public Result runBuild(DynamicBuild build, BuildExecutionContext buildExecutionContext, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        Map<String,Object> buildEnvironment = build.getEnvironmentWithChangeSet(listener);
        Result result = doCheckout(buildEnvironment, buildExecutionContext, listener);
        if (!Result.SUCCESS.equals(result)) {
            return result;
        }
        Map config = new GroovyYamlTemplateProcessor(getDotCiYml(build), buildEnvironment).getConfig();
        this.buildConfiguration = new BuildConfiguration(config);
        if(buildConfiguration.isSkipped()){
            build.skip();
            return Result.SUCCESS;
        }


        build.setAxisList(buildConfiguration.getAxisList());

        if(buildConfiguration.isParallelized()){
            result = runParallelBuild(build, buildExecutionContext, buildConfiguration, listener);
        }else{
            result = runSubBuild(new Combination(ImmutableMap.of("script", buildConfiguration.getOnlyRun())), buildExecutionContext, listener);
        }

        Result pluginResult = runPlugins(build, buildConfiguration.getPlugins(), listener, launcher);
        Result notifierResult = runNotifiers(build, buildConfiguration.getNotifiers(), listener);
        return result.combine(pluginResult).combine(notifierResult);
    }

    @Override
    public Result runSubBuild(Combination combination, BuildExecutionContext buildExecutionContext, BuildListener listener) throws IOException, InterruptedException {
        ShellCommands commands = buildConfiguration.getCommands(combination, buildExecutionContext.getBuildEnvironmentVariables());
        return runCommands(commands, buildExecutionContext, listener);
    }

    private Result doCheckout(Map<String,Object> buildEnvironment, BuildExecutionContext buildExecutionContext, BuildListener listener) throws IOException, InterruptedException {
        ShellCommands commands = BuildConfiguration.getCheckoutCommands(buildEnvironment);
        return runCommands(commands, buildExecutionContext, listener);
    }

    private Result runCommands(ShellCommands commands, BuildExecutionContext buildExecutionContext, BuildListener listener) throws IOException, InterruptedException {
        ShellScriptRunner shellScriptRunner = new ShellScriptRunner(buildExecutionContext, listener);
        return shellScriptRunner.runScript(commands);
    }

    private String getDotCiYml(DynamicBuild build) throws IOException, InterruptedException {
        FilePath fp = new FilePath(build.getWorkspace(), ".ci.yml");
        if (!fp.exists()) {
            throw new InvalidBuildConfigurationException("No .ci.yml found.");
        }

        return fp.readToString();
    }

    private Result runParallelBuild(final DynamicBuild dynamicBuild, final  BuildExecutionContext buildExecutionContext, final BuildConfiguration buildConfiguration, final BuildListener listener) throws IOException, InterruptedException {
        Result beforeRunResult = runBeforeCommands(buildExecutionContext, listener);

        SubBuildScheduler subBuildScheduler = new SubBuildScheduler(dynamicBuild, this, new SubBuildScheduler.SubBuildFinishListener() {
            @Override
            public void runFinished(DynamicSubBuild subBuild) throws IOException {
                for (DotCiPluginAdapter plugin : buildConfiguration.getPlugins()) {
                    plugin.runFinished(subBuild, dynamicBuild, listener);
                }
            }
        });

        try {
            Iterable<Combination> axisList = buildConfiguration.getAxisList().list();
            Result combinedResult = subBuildScheduler.runSubBuilds(axisList, listener);
            dynamicBuild.setResult(combinedResult.combine(beforeRunResult));
            return combinedResult;
        } finally {
            try {
                subBuildScheduler.cancelSubBuilds(listener.getLogger());
            } catch (Exception e) {
                // There is nothing much we can do at this point
            }
        }
    }

    private Result runBeforeCommands(final BuildExecutionContext buildExecutionContext, final BuildListener listener) throws IOException, InterruptedException {
        ShellCommands beforeCommands = buildConfiguration.getBeforeRunCommandIfPresent();
        if (beforeCommands != null) {
            return runCommands(beforeCommands, buildExecutionContext, listener);
        }
        return Result.SUCCESS;
    }

    private Result runPlugins(DynamicBuild dynamicBuild, List<DotCiPluginAdapter> plugins, BuildListener listener, Launcher launcher) {
        boolean result = true ;
        for(DotCiPluginAdapter plugin : plugins){
           result = result & plugin.perform(dynamicBuild, launcher, listener);
        }
        return result? Result.SUCCESS: Result.FAILURE;
    }
    private Result runNotifiers(DynamicBuild build, List<PostBuildNotifier> notifiers, BuildListener listener) {
        boolean result = true ;
        for (PostBuildNotifier notifier : notifiers) {
            result = result & notifier.perform(build, listener);
        }
        return result? Result.SUCCESS: Result.FAILURE;
    }

}
