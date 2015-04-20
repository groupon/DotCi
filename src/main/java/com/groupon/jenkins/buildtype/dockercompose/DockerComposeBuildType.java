package com.groupon.jenkins.buildtype.dockercompose;

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.buildtype.InvalidBuildConfigurationException;
import com.groupon.jenkins.buildtype.docker.CheckoutCommands;
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
public class DockerComposeBuildType extends BuildType implements SubBuildRunner {
    private BuildConfiguration buildConfiguration;

    @Override
    public String getDescription() {
        return "Docker Compose Build";
    }

    @Override
    public Result runBuild(DynamicBuild build, BuildExecutionContext buildExecutionContext, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        Map<String,Object> buildEnvironment = build.getEnvironmentWithChangeSet(listener);
        ShellCommands checkoutCommands = getCheckoutCommands(buildEnvironment);
        Map config = new GroovyYamlTemplateProcessor(getDotCiYml(build), buildEnvironment).getConfig();
        this.buildConfiguration = getBuildConfiguration(build.getParent().getFullName(),config,build.getBuildId(),checkoutCommands,build.getSha(),build.getNumber());
        build.setAxisList(buildConfiguration.getAxisList());
        Result result ;
        if(buildConfiguration.isParallelized()){
            ShellScriptRunner shellScriptRunner = new ShellScriptRunner(buildExecutionContext, listener);
            Result checkoutResult = shellScriptRunner.runScript(checkoutCommands);
            if(Result.FAILURE.equals(checkoutResult)) return checkoutResult;
            result = runMultiConfigbuildRunner(build, buildConfiguration, listener,launcher);
        }else{
            result = runSubBuild(new Combination(ImmutableMap.of("run", buildConfiguration.getOnlyRun())), buildExecutionContext, listener);
        }
        Result pluginResult = runPlugins(build, buildConfiguration.getPlugins(), listener, launcher);
        Result notifierResult = runNotifiers(build, buildConfiguration.getNotifiers(), listener);
        return  result.combine(pluginResult).combine(notifierResult);
    }

    private BuildConfiguration getBuildConfiguration(String fullName, Map config, String buildId, ShellCommands checkoutCommands , String sha, int number) {
        return new BuildConfiguration(fullName,config,buildId,checkoutCommands,sha,number);
    }
    public ShellCommands getCheckoutCommands(Map<String, Object> dotCiEnvVars) {
        GitUrl gitRepoUrl = new GitUrl((String) dotCiEnvVars.get("GIT_URL"));
        String gitUrl = gitRepoUrl.getHttpsUrl();
        ShellCommands shellCommands = new ShellCommands();
        shellCommands.add("find . -delete");
        if(dotCiEnvVars.get("DOTCI_PULL_REQUEST") != null){
            shellCommands.add(format("git clone %s %s",gitUrl,"."));
            shellCommands.add(format("git fetch origin \"+refs/pull/%s/merge:\"", dotCiEnvVars.get("DOTCI_PULL_REQUEST")));
            shellCommands.add("git reset --hard FETCH_HEAD");
        }else {
            shellCommands.add(format("git clone --branch=%s %s %s", dotCiEnvVars.get("DOTCI_BRANCH"), gitUrl,"."));
            shellCommands.add(format("git reset --hard  %s", dotCiEnvVars.get("SHA")));
        }
        return shellCommands;
    }
    @Override
    public Result runSubBuild(Combination combination, BuildExecutionContext buildExecutionContext, BuildListener listener) throws IOException, InterruptedException {
        ShellScriptRunner shellScriptRunner = new ShellScriptRunner(buildExecutionContext, listener);
        return shellScriptRunner.runScript(buildConfiguration.getCommands(combination));
    }
    private String getDotCiYml(DynamicBuild build) throws IOException {
        try {
            return build.getGithubRepositoryService().getGHFile(".ci.yml", build.getSha()).getContent();
        } catch (FileNotFoundException _){
            throw new InvalidBuildConfigurationException("No .ci.yml found.");
        }
    }

    private Result runMultiConfigbuildRunner(DynamicBuild dynamicBuild, final BuildConfiguration buildConfiguration, final BuildListener listener, Launcher launcher) throws IOException, InterruptedException {
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
            }
        }
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
