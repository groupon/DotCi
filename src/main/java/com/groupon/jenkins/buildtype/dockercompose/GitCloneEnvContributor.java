package com.groupon.jenkins.buildtype.dockercompose;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.git.GitUrl;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Job;
import hudson.model.TaskListener;

import java.io.IOException;

@Extension
public class GitCloneEnvContributor extends EnvironmentContributor {
    @Override
    public void buildEnvironmentFor(Job job, EnvVars envs, TaskListener listener) throws IOException, InterruptedException {
        if (job instanceof DynamicProject) {
            DynamicProject dynamicJob = ((DynamicProject) job);
            GitUrl gitUrl = new GitUrl(dynamicJob.getGithubRepoUrl());
            envs.put("DOTCI_DOCKER_COMPOSE_GIT_CLONE_URL", getCloneUrl(gitUrl));
        }
    }

    private String getCloneUrl(GitUrl gitUrl) {
        String cloneTemplate = GlobalConfiguration.get().getCloneUrlTemplate();
        return gitUrl.applyTemplate(cloneTemplate);
    }
}
