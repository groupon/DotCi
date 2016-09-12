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
package com.groupon.jenkins.dynamic.build.cause;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class BuildCauseTest {

    @Test
    public void should_export_env_vars() {
        final BuildCause cause = mock(BuildCause.class);
        doCallRealMethod().when(cause).getEnvVars();

        doReturn("sha1234").when(cause).getSha();
        doReturn("surya").when(cause).getPusher();
        doReturn("44").when(cause).getPullRequestNumber();
        assertNotNull(cause.getEnvVars());
        assertEquals("sha1234", cause.getEnvVars().get("DOTCI_SHA"));
        assertEquals("surya", cause.getEnvVars().get("DOTCI_PUSHER"));
        assertEquals("44", cause.getEnvVars().get("DOTCI_PULL_REQUEST"));
    }

    @Test
    public void should_export_pr_env_vars() {
        final GitHubPullRequestCause cause = mock(GitHubPullRequestCause.class);
        doCallRealMethod().when(cause).getEnvVars();
        doCallRealMethod().when(cause).getCauseEnvVars();

        doReturn("feature").when(cause).getSourceBranch();
        doReturn("master").when(cause).getTargetBranch();

        assertNotNull(cause.getEnvVars());
        assertEquals("feature", cause.getEnvVars().get("DOTCI_PULL_REQUEST_SOURCE_BRANCH"));
        assertEquals("master", cause.getEnvVars().get("DOTCI_PULL_REQUEST_TARGET_BRANCH"));
    }

    @Test
    public void should_export_not_export_null_env_vars() {
        final BuildCause cause = mock(BuildCause.class);
        doCallRealMethod().when(cause).getEnvVars();

        doReturn(null).when(cause).getPullRequestNumber();
        assertNotNull(cause.getEnvVars());
        assertFalse(cause.getEnvVars().keySet().contains("DOTCI_PULL_REQUEST"));
    }

}
