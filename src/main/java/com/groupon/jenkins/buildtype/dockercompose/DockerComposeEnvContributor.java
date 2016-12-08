/*
The MIT License (MIT)

Copyright (c) 2015, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package com.groupon.jenkins.buildtype.dockercompose;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicSubBuild;
import com.groupon.jenkins.dynamic.buildtype.BuildType;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;
import java.util.logging.Logger;

@Extension
public class DockerComposeEnvContributor extends EnvironmentContributor {
    public final static String COMPOSE_PROJECT_NAME = "COMPOSE_PROJECT_NAME";
    private static final Logger LOGGER = Logger.getLogger(DockerComposeEnvContributor.class.getName());

    @Override
    public void buildEnvironmentFor(Run run, EnvVars envs, TaskListener listener) throws IOException, InterruptedException {
        if (isDockerComposeBuild(run)) {
            String composeProjectName = String.format("%s%s", run.getParent().getFullName(), run.getNumber())
                .replaceAll("[^A-Za-z0-9]", "").toLowerCase();
            LOGGER.fine("Setting COMPOSE_PROJECT_NAME=" + composeProjectName);
            envs.put(COMPOSE_PROJECT_NAME, composeProjectName);
        }
    }

    private boolean isDockerComposeBuild(Run run) {
        DynamicProject project = null;
        if (run instanceof DynamicSubBuild) {
            project = ((DynamicSubBuild) run).getParent().getParent();
        } else if (run instanceof DynamicBuild) {
            project = ((DynamicBuild) run).getParent();
        }
        return project != null && BuildType.isProjectOfBuildType(project, DockerComposeBuild.class);
    }
}
