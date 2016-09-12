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
import com.groupon.jenkins.dynamic.build.cause.GithubPushPullWebhookCause;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterValue;
import hudson.security.ACL;
import org.acegisecurity.context.SecurityContextHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHEvent;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.util.Arrays;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
        this.githubWebhook.doIndex(mock(StaplerRequest.class), null);
    }

    @Test
    public void should_get_payload_from_post_if_post() throws IOException, InterruptedException {
        final StaplerRequest request = mock(StaplerRequest.class);
        when(request.getParameter("payload")).thenReturn("{repository: { url: 'git@repo' },deleted: false}");
        when(request.getHeader("X-GitHub-Event")).thenReturn(GHEvent.PUSH.toString());
        when(request.getMethod()).thenReturn("POST");
        final DynamicProject project = mock(DynamicProject.class);
        final ParametersDefinitionProperty paramDefinition = mock(ParametersDefinitionProperty.class);
        when(project.getProperty(ParametersDefinitionProperty.class)).thenReturn(paramDefinition);

        kickOffBuildTrigger(request, project);

        verify(project).scheduleBuild(eq(0), any(GithubPushPullWebhookCause.class), any(ParametersAction.class));
    }

    @Test
    public void should_trigger_builds_for_payload() throws IOException, InterruptedException {
        final StaplerRequest request = mock(StaplerRequest.class);
        when(request.getParameter("payload")).thenReturn("payload");
        final DynamicProject project = mock(DynamicProject.class);
        final ParametersDefinitionProperty paramDefinition = mock(ParametersDefinitionProperty.class);
        when(project.getProperty(ParametersDefinitionProperty.class)).thenReturn(paramDefinition);

        kickOffBuildTrigger(request, project);

        verify(project).scheduleBuild(eq(0), any(GithubPushPullWebhookCause.class), any(ParametersAction.class));
    }

    @Test
    public void should_trigger_build_with_default_parameter_values() throws IOException, InterruptedException {
        final StaplerRequest request = mock(StaplerRequest.class);
        when(request.getParameter("payload")).thenReturn("payload");
        final DynamicProject project = mock(DynamicProject.class);

        final ParameterDefinition branchParameter = mock(ParameterDefinition.class);
        when(branchParameter.getName()).thenReturn("BRANCH");

        final ParameterDefinition secondParameter = mock(ParameterDefinition.class);
        when(branchParameter.getName()).thenReturn("PARAM");
        when(secondParameter.getDefaultParameterValue()).thenReturn(new StringParameterValue("PARAM", "meow"));
        final ParametersDefinitionProperty paramDefinition = mock(ParametersDefinitionProperty.class);
        when(paramDefinition.getParameterDefinitions()).thenReturn(Arrays.asList(branchParameter, secondParameter));
        when(project.getProperty(ParametersDefinitionProperty.class)).thenReturn(paramDefinition);

        kickOffBuildTrigger(request, project);

        final ArgumentCaptor<ParametersAction> parametersCaptor = ArgumentCaptor.forClass(ParametersAction.class);
        verify(project).scheduleBuild(eq(0), any(GithubPushPullWebhookCause.class), parametersCaptor.capture());

        final ParametersAction parametersAction = parametersCaptor.getValue();
        Assert.assertTrue(parametersAction.getParameter("PARAM") instanceof StringParameterValue);
        Assert.assertEquals("meow", ((StringParameterValue) parametersAction.getParameter("PARAM")).value);

    }

    @Test
    public void should_authenticate_as_SYSTEM() throws IOException, InterruptedException {
        final StaplerRequest request = mock(StaplerRequest.class);
        final DynamicProject project = mock(DynamicProject.class);
        when(request.getParameter("payload")).thenReturn("payload");
        kickOffBuildTrigger(request, project);
        Assert.assertEquals(ACL.SYSTEM, SecurityContextHolder.getContext().getAuthentication());
    }


    protected void kickOffBuildTrigger(final StaplerRequest request, final DynamicProject projectForRepo) throws IOException, InterruptedException {
        final PushAndPullRequestPayload payload = mock(PushAndPullRequestPayload.class);

        final DynamicProjectRepository projectRepo = mock(DynamicProjectRepository.class);

        when(payload.needsBuild(any(DynamicProject.class))).thenReturn(true);
        when(payload.getProjectUrl()).thenReturn("git@repo");
        when(payload.getCause()).thenReturn(mock(GithubPushPullWebhookCause.class));
        when(projectRepo.getJobsFor("git@repo")).thenReturn(newArrayList(projectForRepo));

        doReturn(payload).when(this.githubWebhook).makePayload(anyString(), anyString());
        doReturn(projectRepo).when(this.githubWebhook).makeDynamicProjectRepo();

        this.githubWebhook.doIndex(request, null);
        Thread.sleep(2000);
    }

}
