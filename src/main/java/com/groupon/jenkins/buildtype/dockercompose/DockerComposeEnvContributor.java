package com.groupon.jenkins.buildtype.dockercompose;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicSubBuild;
import com.groupon.jenkins.dynamic.build.DynamicSubProject;
import com.groupon.jenkins.git.GitUrl;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;
import java.util.logging.Logger;

@Extension
public class DockerComposeEnvContributor extends EnvironmentContributor {
    private static final Logger LOGGER = Logger.getLogger(DockerComposeEnvContributor.class.getName());

    @Override
    public void buildEnvironmentFor(Run run, EnvVars envs, TaskListener listener) throws IOException, InterruptedException {
        if(run instanceof DynamicSubBuild) {
            DynamicSubBuild build = ((DynamicSubBuild) run);

            String composeProjectName = String.format("%s%s", build.getParent().getFullName(), build.getNumber())
                    .replaceAll("[^A-Za-z0-9]", "").toLowerCase();

            LOGGER.info("Setting COMPOSE_PROJECT_NAME=" + composeProjectName);
            envs.put("COMPOSE_PROJECT_NAME", composeProjectName);
        }
    }
}
