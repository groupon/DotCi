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

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.groupon.jenkins.SetupConfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DotCiGithubOauthScopeTest {

    private DotCiGitHubOAuthScope dotCiScope;
    private SetupConfig setupConfig;

    @Before
    public void setup() {
        dotCiScope = spy(new DotCiGitHubOAuthScope());
        setupConfig = mock(SetupConfig.class);
        doReturn(setupConfig).when(dotCiScope).getSetupConfig();
    }

    @Test
    public void should_ask_for_private_repo_permssions_on_if_setup_in_config() {
        when(setupConfig.hasPrivateRepoSupport()).thenReturn(true);
        Collection<String> scopes = dotCiScope.getScopesToRequest();
        assertTrue(scopes.contains("repo"));
    }

    @Test
    public void should_not_ask_for_private_repo_permssions_on_if_not_setup_in_config() {
        when(setupConfig.hasPrivateRepoSupport()).thenReturn(false);
        Collection<String> scopes = dotCiScope.getScopesToRequest();
        assertFalse(scopes.contains("repo"));
    }

}
