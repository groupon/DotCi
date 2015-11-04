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
import com.groupon.jenkins.testhelpers.DynamicBuildFactory;
import hudson.EnvVars;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DockerComposeEnvContributorTest {
    private DockerComposeEnvContributor contributor = new DockerComposeEnvContributor();

    @Test
    public void should_set_compose_project_name() throws Exception {
        final EnvVars envVars = new EnvVars();
        envVars.put("JOB_NAME", "groupon/DotCi");
        DynamicBuild build = DynamicBuildFactory.newBuild().get();
        when(build.getNumber()).thenReturn(8);
        DynamicProject project = mock(DynamicProject.class);
        when(project.getBuildType()).thenReturn(DockerComposeBuild.class.getName());
        when(build.getParent()).thenReturn(project);
        contributor.buildEnvironmentFor(build, envVars, null);

        assertEquals("groupondotci8", envVars.get(DockerComposeEnvContributor.COMPOSE_PROJECT_NAME));
    }

    @Test
    public void should_set_compose_project_name_for_subbuild() throws Exception {
        final EnvVars envVars = new EnvVars();
        envVars.put("JOB_NAME", "groupon/DotCi/script=test");

        DynamicSubBuild build = mock(DynamicSubBuild.class);
        when(build.getNumber()).thenReturn(8);

        DynamicBuild parentBuild = DynamicBuildFactory.newBuild()
                .withSubBuilds(build).get();
        DynamicProject parentProject = parentBuild.getParent();
        when(parentProject.getBuildType()).thenReturn(DockerComposeBuild.class.getName());

        contributor.buildEnvironmentFor(build, envVars, null);
        assertEquals("groupondotciscripttest8", envVars.get(DockerComposeEnvContributor.COMPOSE_PROJECT_NAME));
    }

    @Test
    public void should_not_set_compose_project_name_for_non_docker_compose_build() throws Exception {
        final EnvVars envVars = new EnvVars();
        envVars.put("JOB_NAME", "groupon/DotCi/script=test");

        DynamicSubBuild build = mock(DynamicSubBuild.class);
        when(build.getNumber()).thenReturn(8);

        DynamicBuild parentBuild = DynamicBuildFactory.newBuild()
                .withSubBuilds(build).get();
        DynamicProject parentProject = parentBuild.getParent();
        when(parentProject.getBuildType()).thenReturn("com.other.BuildType");

        contributor.buildEnvironmentFor(build, envVars, null);
        assertNull(envVars.get(DockerComposeEnvContributor.COMPOSE_PROJECT_NAME));
    }

}