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

package com.groupon.jenkins.buildtype.docker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
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
import com.groupon.jenkins.notifications.PostBuildNotifier;
import com.groupon.jenkins.util.GroovyYamlTemplateProcessor;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.matrix.Combination;
import hudson.model.BuildListener;
import hudson.model.Result;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DockerBuild extends BuildType implements SubBuildRunner {
    private static final Logger LOGGER = Logger.getLogger(DockerBuild.class.getName());
    private DockerBuildConfiguration buildConfiguration;

    public abstract DockerBuildConfiguration getBuildConfiguration(Map config, String buildId, EnvVars buildEnvironment) ;

    @Override
    public Result runBuild(DynamicBuild build, BuildExecutionContext buildExecutionContext, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        try{
            EnvVars buildEnvironment = build.getEnvironment(listener);
            Map config = new GroovyYamlTemplateProcessor(getDotCiYml(build), buildEnvironment).getConfig();
            this.buildConfiguration = getBuildConfiguration(config,build.getBuildId(),buildEnvironment);
            build.setAxisList(buildConfiguration.getAxisList());
            Result result ;
            if(buildConfiguration.isParallized()){
                result = runMultiConfigbuildRunner(build, buildConfiguration, listener, launcher);;
            }else{
                result = runSubBuild(new Combination(ImmutableMap.of("script", "main")), buildExecutionContext, listener) ;
            }
            runPlugins(build, buildConfiguration.getPlugins(), listener, launcher);
            runNotifiers(build,buildConfiguration.getNotifiers(),listener);
            return result;

        }catch (InterruptedException e){
            if(buildConfiguration !=null && Iterables.isEmpty(buildConfiguration.getLinkCleanupCommands())){
                ShellCommands cleanupCommands = new ShellCommands();
                cleanupCommands.addAll(buildConfiguration.getLinkCleanupCommands());
                new ShellScriptRunner(buildExecutionContext, listener).runScript(cleanupCommands);
            }
           throw e;
        }
    }
    private void runPlugins(DynamicBuild dynamicBuild, List<DotCiPluginAdapter> plugins, BuildListener listener, Launcher launcher) {
        for(DotCiPluginAdapter plugin : plugins){
            plugin.perform(dynamicBuild, launcher, listener);
        }
    }
    private boolean runNotifiers(DynamicBuild build,  List<PostBuildNotifier> notifiers , BuildListener listener) {
        boolean result = true ;
        for (PostBuildNotifier notifier : notifiers) {
            result = result & notifier.perform(build, listener);
        }
        return result;
    }

    private Result runMultiConfigbuildRunner(DynamicBuild dynamicBuild, DockerBuildConfiguration buildConfiguration, BuildListener listener, Launcher launcher) throws IOException, InterruptedException {
        SubBuildScheduler subBuildScheduler = new SubBuildScheduler(dynamicBuild, this, new SubBuildScheduler.SubBuildFinishListener() {
            @Override
            public void runFinished(DynamicSubBuild subBuild) throws IOException {
            }
        });

        try {
            Iterable<Combination> axisList = buildConfiguration.getAxisList().list();
            Result combinedResult = subBuildScheduler.runSubBuilds(axisList, listener);
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

    private String getDotCiYml(DynamicBuild build) throws IOException {
       try {
           return build.getGithubRepositoryService().getGHFile(".ci.yml", build.getSha()).getContent();
       } catch (FileNotFoundException _){
           throw new InvalidBuildConfigurationException("No .ci.yml found.");
       }
   }

    @Override
    public Result runSubBuild(Combination combination, BuildExecutionContext buildExecutionContext, BuildListener listener) throws IOException, InterruptedException {
        return new ShellScriptRunner(buildExecutionContext, listener).runScript(buildConfiguration.toShellCommands(combination));
    }
}
