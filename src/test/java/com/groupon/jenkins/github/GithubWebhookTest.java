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
package com.groupon.jenkins.github;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.cause.GithubCause;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import hudson.model.ParametersAction;
import hudson.security.ACL;
import java.io.IOException;
import org.acegisecurity.context.SecurityContextHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GithubWebhookTest {

    @Spy
    GithubWebhook githubWebhook = new GithubWebhook();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_be_called_with_payload() throws IOException {
        githubWebhook.doIndex(mock(StaplerRequest.class), null);
    }

    @Test
    public void should_get_payload_from_post_if_post() throws IOException, InterruptedException {
        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getParameter("payload")).thenReturn(null);
        when(request.getMethod()).thenReturn("POST");
        doReturn("payload").when(githubWebhook).getRequestPayload(request);
        verifyBuildTrigger(request);
    }

    @Test
    public void should_trigger_builds_for_payload() throws IOException, InterruptedException {
        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getParameter("payload")).thenReturn("payload");
        verifyBuildTrigger(request);
    }

    @Test
    public void should_authenticate_as_SYSTEM() throws IOException, InterruptedException {
        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getParameter("payload")).thenReturn("payload");
        verifyBuildTrigger(request);
        Assert.assertEquals(ACL.SYSTEM, SecurityContextHolder.getContext().getAuthentication());
    }

    protected void verifyBuildTrigger(StaplerRequest request) throws IOException, InterruptedException {
        Payload payload = mock(Payload.class);

        DynamicProject projectForRepo = mock(DynamicProject.class);
        DynamicProjectRepository projectRepo = mock(DynamicProjectRepository.class);

        when(payload.needsBuild()).thenReturn(true);
        when(payload.getProjectUrl()).thenReturn("git@repo");
        when(projectRepo.getJobsFor("git@repo")).thenReturn(newArrayList(projectForRepo));

        doReturn(payload).when(githubWebhook).makePayload("payload");
        doReturn(projectRepo).when(githubWebhook).makeDynamicProjectRepo();

        githubWebhook.doIndex(request, null);
        Thread.sleep(2000);
        verify(projectForRepo).scheduleBuild(eq(0), any(GithubCause.class), any(ParametersAction.class));
    }

}
