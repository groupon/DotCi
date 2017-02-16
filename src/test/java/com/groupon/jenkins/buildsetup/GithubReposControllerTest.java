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
package com.groupon.jenkins.buildsetup;

import com.google.common.collect.Iterators;
import com.groupon.jenkins.github.services.GithubCurrentUserService;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubReposControllerTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void should_show_repos_with_admin_access_only() throws IOException {
        final GithubCurrentUserService githubCurrentUser = mock(GithubCurrentUserService.class);
        final GHRepository repoWithAdminAccess = mock(GHRepository.class);
        when(repoWithAdminAccess.hasAdminAccess()).thenReturn(true);
        final GHRepository repoWithoutAdminAcess = mock(GHRepository.class);

        when(repoWithAdminAccess.getOwnerName()).thenReturn("meow");
        when(repoWithoutAdminAcess.getOwnerName()).thenReturn("meow");

        when(githubCurrentUser.getRepositories("meow")).thenReturn(Arrays.asList(repoWithoutAdminAcess, repoWithAdminAccess));
        final GithubReposController controller = new GithubReposController() {
            @Override
            protected GithubCurrentUserService getCurrentUser() {
                return githubCurrentUser;
            }

            @Override
            public String getCurrentOrg() {
                return "meow";
            }
        };
        assertEquals(1, Iterators.size(controller.getRepositories()));
    }
}
