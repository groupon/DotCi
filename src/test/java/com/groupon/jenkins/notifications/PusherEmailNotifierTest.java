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
package com.groupon.jenkins.notifications;

import hudson.model.BuildListener;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.cause.GitHubPushCause;
import com.groupon.jenkins.dynamic.build.cause.GithubPushPullWebhookCause;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PusherEmailNotifierTest {

    private BuildListener listener;
    private DynamicBuild build;

    @Before
    public void setupMocks() throws IOException, InterruptedException {
        build = mock(DynamicBuild.class);
        listener = mock(BuildListener.class);
    }

    @Test
    public void should_attempt_sending_an_email_only_if_github_push() {
        PusherEmailNotifier pusherEmailNotifier = new PusherEmailNotifier();
        when(build.getCause(GithubPushPullWebhookCause.class)).thenReturn(null);
        assertFalse(pusherEmailNotifier.needsEmail(build, listener));
    }

    @Test
    public void should_not_attempt_sending_an_email_only_if_github_push_and_email_not_configured() {
        PusherEmailNotifier pusherEmailNotifier = new PusherEmailNotifier();
        GitHubPushCause githubCause = mock(GitHubPushCause.class);
        when(githubCause.getPusherEmailAddress()).thenReturn(null);
        when(build.getCause(GitHubPushCause.class)).thenReturn(githubCause);
        assertFalse(pusherEmailNotifier.needsEmail(build, listener));
    }

    @Test
    public void should_attempt_sending_an_email_only_if_github_push_and_email_configured() {
        PusherEmailNotifier pusherEmailNotifier = new PusherEmailNotifier();
        GitHubPushCause githubCause = mock(GitHubPushCause.class);
        when(githubCause.getPusherEmailAddress()).thenReturn("surya@groupon.com");
        when(build.getCause(GitHubPushCause.class)).thenReturn(githubCause);
        assertTrue(pusherEmailNotifier.needsEmail(build, listener));
    }
}
