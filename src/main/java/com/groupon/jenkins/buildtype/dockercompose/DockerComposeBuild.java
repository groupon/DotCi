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

import com.google.common.collect.Iterables;
import com.groupon.jenkins.buildtype.InvalidBuildConfigurationException;
import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import com.groupon.jenkins.buildtype.util.shell.ShellScriptRunner;
import com.groupon.jenkins.dynamic.build.DotCiBuildInfoAction;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicSubBuild;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import com.groupon.jenkins.dynamic.build.execution.SubBuildRunner;
import com.groupon.jenkins.dynamic.build.execution.SubBuildScheduler;
import com.groupon.jenkins.dynamic.buildtype.BuildType;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import com.groupon.jenkins.util.GroovyYamlTemplateProcessor;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.Combination;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Extension
public class DockerComposeBuild extends BuildType implements SubBuildRunner {
    private BuildConfiguration buildConfiguration;

    @Override
    public String getDescription() {
        return "Docker Compose Build";
    }

    @Override
    public Result runBuild(final DynamicBuild build, final BuildExecutionContext buildExecutionContext, final Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
        build.save();
        final Map<String, Object> buildEnvironment = build.getEnvironmentWithChangeSet(listener);
        Result result = doCheckout(buildEnvironment, buildExecutionContext, listener);
        if (!Result.SUCCESS.equals(result)) {
            return result;
        }
        final Map config = new GroovyYamlTemplateProcessor(getDotCiYml(build), buildEnvironment).getConfig();
        addProcessedYamlToDotCiInfoAction(buildExecutionContext.getRun(), config);
        this.buildConfiguration = new BuildConfiguration(config);
        if (this.buildConfiguration.isSkipped()) {
            build.skip();
            return Result.SUCCESS;
        }
        result = runBeforeCommands(buildExecutionContext, listener);
        if (Result.SUCCESS.equals(result)) {
            build.setAxisList(this.buildConfiguration.getAxisList());
            if (this.buildConfiguration.isParallelized()) {
                result = runParallelBuild(build, buildExecutionContext, this.buildConfiguration, listener);
            } else {
                result = runSubBuild(Iterables.getOnlyElement(this.buildConfiguration.getAxisList().list()), buildExecutionContext, listener);
            }
        }
        final Result pluginResult = runPlugins(build, this.buildConfiguration.getPlugins(), listener, launcher);
        final Result notifierResult = runNotifiers(build, this.buildConfiguration.getNotifiers(), listener);
        return result.combine(pluginResult).combine(notifierResult);
    }

    private void addProcessedYamlToDotCiInfoAction(final Run run, final Map config) throws IOException {
        final DotCiBuildInfoAction dotCiBuildInfoAction = run.getAction(DotCiBuildInfoAction.class);
        if (dotCiBuildInfoAction == null) {
            run.addAction(new DotCiBuildInfoAction(new Yaml().dump(config)));
        } else {
            dotCiBuildInfoAction.setBuildConfiguration(new Yaml().dump(config));
        }
        run.save();
    }

    @Override
    public Result runSubBuild(final Combination combination, final BuildExecutionContext buildExecutionContext, final BuildListener listener) throws IOException, InterruptedException {
        final List<ShellCommands> commands = this.buildConfiguration.getCommands(combination, buildExecutionContext.getBuildEnvironmentVariables());
        return runCommands(commands, buildExecutionContext, listener);
    }


    private Result doCheckout(final Map<String, Object> buildEnvironment, final BuildExecutionContext buildExecutionContext, final BuildListener listener) throws IOException, InterruptedException {
        final ShellCommands commands = BuildConfiguration.getCheckoutCommands(buildEnvironment);
        return runCommands(commands, buildExecutionContext, listener);
    }

    private Result runCommands(final List<ShellCommands> commandList, final BuildExecutionContext buildExecutionContext, final BuildListener listener) throws IOException, InterruptedException {
        final Result result = Result.SUCCESS;
        for (final ShellCommands commands : commandList) {
            result.combine(runCommands(commands, buildExecutionContext, listener));
        }
        return result;
    }

    private Result runCommands(final ShellCommands commands, final BuildExecutionContext buildExecutionContext, final BuildListener listener) throws IOException, InterruptedException {
        if (commands == null) {
            return Result.SUCCESS;
        }
        final ShellScriptRunner shellScriptRunner = new ShellScriptRunner(buildExecutionContext, listener);
        return shellScriptRunner.runScript(commands);
    }

    private String getDotCiYml(final DynamicBuild build) throws IOException, InterruptedException {
        final FilePath fp = new FilePath(build.getWorkspace(), ".ci.yml");
        if (!fp.exists()) {
            throw new InvalidBuildConfigurationException("No .ci.yml found.");
        }

        return fp.readToString();
    }

    private Result runParallelBuild(final DynamicBuild dynamicBuild, final BuildExecutionContext buildExecutionContext, final BuildConfiguration buildConfiguration, final BuildListener listener) throws IOException, InterruptedException {

        final SubBuildScheduler subBuildScheduler = new SubBuildScheduler(dynamicBuild, this, new SubBuildScheduler.SubBuildFinishListener() {
            @Override
            public void runFinished(final DynamicSubBuild subBuild) throws IOException {
                for (final DotCiPluginAdapter plugin : buildConfiguration.getPlugins()) {
                    plugin.runFinished(subBuild, dynamicBuild, listener);
                }
            }
        });

        try {
            final Iterable<Combination> axisList = buildConfiguration.getAxisList().list();
            Result runResult = subBuildScheduler.runSubBuilds(axisList, listener);
            if (runResult.equals(Result.SUCCESS)) {
                final Result afterRunResult = runAfterCommands(buildExecutionContext, listener);
                runResult = runResult.combine(afterRunResult);
            }
            dynamicBuild.setResult(runResult);
            return runResult;
        } finally {
            try {
                subBuildScheduler.cancelSubBuilds(listener.getLogger());
            } catch (final Exception e) {
                // There is nothing much we can do at this point
            }
        }
    }

    private Result runAfterCommands(final BuildExecutionContext buildExecutionContext, final BuildListener listener) throws IOException, InterruptedException {
        return runCommands(this.buildConfiguration.getAfterRunCommandsIfPresent(), buildExecutionContext, listener);
    }

    private Result runBeforeCommands(final BuildExecutionContext buildExecutionContext, final BuildListener listener) throws IOException, InterruptedException {
        return runCommands(this.buildConfiguration.getBeforeRunCommandsIfPresent(), buildExecutionContext, listener);
    }

    private Result runPlugins(final DynamicBuild dynamicBuild, final List<DotCiPluginAdapter> plugins, final BuildListener listener, final Launcher launcher) {
        boolean result = true;
        for (final DotCiPluginAdapter plugin : plugins) {
            result = result & plugin.perform(dynamicBuild, launcher, listener);
        }
        return result ? Result.SUCCESS : Result.FAILURE;
    }

    private Result runNotifiers(final DynamicBuild build, final List<PostBuildNotifier> notifiers, final BuildListener listener) {
        boolean result = true;
        for (final PostBuildNotifier notifier : notifiers) {
            result = result & notifier.perform(build, listener);
        }
        return result ? Result.SUCCESS : Result.FAILURE;
    }

}
