/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

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
package com.groupon.jenkins.dynamic.build;

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import hudson.EnvVars;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DbBackedBuildTest {

    @Test
    public void should_export_env_vars() throws IOException, InterruptedException {
        DbBackedBuild dynamicBuild = mock(DbBackedBuild.class, CALLS_REAL_METHODS);
        BuildCause buildCause = mock(BuildCause.class);
        when(buildCause.getEnvVars()).thenReturn(ImmutableMap.of("ENV1", "env1"));

        doReturn(buildCause).when(dynamicBuild).getCause();
        doReturn(new EnvVars("BRANCH", "master")).when(dynamicBuild).getJenkinsEnvVariables(null);
        assertNotNull(dynamicBuild.getEnvironment(null));
        assertEquals("master", dynamicBuild.getEnvironment(null).get("DOTCI_BRANCH"));
        assertEquals("env1", dynamicBuild.getEnvironment(null).get("ENV1"));
        assertEquals("true", dynamicBuild.getEnvironment(null).get("DOTCI"));
        assertEquals("true", dynamicBuild.getEnvironment(null).get("CI"));
    }

}
