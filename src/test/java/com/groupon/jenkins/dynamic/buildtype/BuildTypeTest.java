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

package com.groupon.jenkins.dynamic.buildtype;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.buildtype.dockerimage.DockerImageBuildType;
import com.groupon.jenkins.buildtype.install_packages.InstallPackagesBuildType;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

public class BuildTypeTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();
    @Test
    public void should_use_project_buildtype_if_available() throws Exception {
        String dockerImageBuildType = new DockerImageBuildType().getId();
        String installPackagesBuildType = new InstallPackagesBuildType().getId();
        SetupConfig.get().setDefaultBuildType(dockerImageBuildType);
        DynamicProject dynamicProject = Mockito.mock(DynamicProject.class);
        Mockito.when(dynamicProject.getBuildType()).thenReturn(installPackagesBuildType);
        BuildType buildType = BuildType.getBuildType(dynamicProject);
        Assert.assertEquals(installPackagesBuildType,buildType.getId());
    }
}