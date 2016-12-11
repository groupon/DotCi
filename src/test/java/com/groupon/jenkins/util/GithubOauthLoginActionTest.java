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

package com.groupon.jenkins.util;

import com.groupon.jenkins.SetupConfig;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GithubOauthLoginActionTest {
    private GithubOauthLoginAction githubOauthLoginAction;
    private SetupConfig setupConfig;
    private HttpPoster httpPoster;

    @Before
    public void setup() throws IOException {
        this.httpPoster = mock(HttpPoster.class);
        this.githubOauthLoginAction = spy(new GithubOauthLoginAction(this.httpPoster));
        this.setupConfig = mock(SetupConfig.class);
        doNothing().when(this.githubOauthLoginAction).updateOfflineAccessTokenForUser(anyString());
        doReturn(this.setupConfig).when(this.githubOauthLoginAction).getSetupConfig();
        doReturn("http://localhost:8080/jenkins").when(this.githubOauthLoginAction).getJenkinsRootUrl();
        when(this.setupConfig.getGithubApiUrl()).thenReturn("githubApiUrl");
        when(this.setupConfig.getGithubWebUrl()).thenReturn("githubWebUrl");
        when(this.setupConfig.getGithubClientID()).thenReturn("githubClientId");
        when(this.setupConfig.getGithubClientSecret()).thenReturn("githubClientSecret");

    }

    @Test
    public void should_ask_for_private_repo_permssions_on_if_setup_in_config() {
        when(this.setupConfig.hasPrivateRepoSupport()).thenReturn(true);
        final List<String> scopes = Arrays.asList(this.githubOauthLoginAction.getScopes().split(","));
        assertTrue(scopes.contains("repo"));
    }

    @Test
    public void should_not_ask_for_private_repo_permssions_on_if_not_setup_in_config() {
        when(this.setupConfig.hasPrivateRepoSupport()).thenReturn(false);
        final List<String> scopes = Arrays.asList(this.githubOauthLoginAction.getScopes().split(","));
        assertFalse(scopes.contains("repo,"));
    }

    @Test
    public void should_set_access_token_in_the_session() throws IOException {
        final StaplerRequest request = mock(StaplerRequest.class);
        final HttpSession httpSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(httpSession);
        when(request.getParameter("code")).thenReturn("code");
        when(this.httpPoster.post(anyString(), anyMap())).thenReturn("access_token=meow_token");
        final HttpResponse response = this.githubOauthLoginAction.doFinishLogin(request, null);

        verify(httpSession).setAttribute("access_token", "meow_token");
        assertNotNull(response);

    }

}
