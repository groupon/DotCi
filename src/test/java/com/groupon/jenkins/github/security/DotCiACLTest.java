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

package com.groupon.jenkins.github.security;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import hudson.security.Permission;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.GithubAuthenticationToken;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DotCiACLTest {

    @Test
    public void should_allow_admins_to_do_anything() throws Exception {
        DotCiACL acl = new DotCiACL("suryagaddipati", false);
        Assert.assertTrue(acl.hasPermission(getGithubAuthentication("suryagaddipati"), Permission.DELETE));
    }

    @Test
    public void should_allow_read_permission_for_everyone_on_non_dotci() throws Exception {
        DotCiACL acl = new DotCiACL("suryagaddipati", false);
        Assert.assertFalse(acl.hasPermission(getGithubAuthentication("chairman-meow"), Permission.DELETE));
        Assert.assertTrue(acl.hasPermission(getGithubAuthentication("chairman-meow"), Jenkins.READ));
    }

    @Test
    public void should_allow_read_permission_for_authenticated_users_if_enabled() throws Exception {
        DotCiACL acl = new DotCiACL(Mockito.mock(OrganizationContainer.class), "suryagaddipati", true);
        Assert.assertFalse(acl.hasPermission(getGithubAuthentication("chairman-meow"), Permission.DELETE));
        Assert.assertTrue(acl.hasPermission(getGithubAuthentication("chairman-meow"), Jenkins.READ));
    }

    @Test
    public void should_allow_permission_on_github_org_only_if_member() throws Exception {
        OrganizationContainer organizationContainer = Mockito.mock(OrganizationContainer.class);
        Mockito.when(organizationContainer.getName()).thenReturn("chairman-meow");
        DotCiACL acl = new DotCiACL(organizationContainer, "suryagaddipati", false);
        GithubAuthenticationToken githubAuthentication = getGithubAuthentication("chairman-meow");
        Assert.assertTrue(acl.hasPermission(githubAuthentication, Jenkins.READ));
    }
    @Test
    public void should_allow_all_permission_on_github_org_only_if_member() throws Exception {
        DynamicProject project = Mockito.mock(DynamicProject.class);
        Mockito.when(project.getGithubRepoUrl()).thenReturn("https://github.com/groupon/DotCi");
        DotCiACL acl = new DotCiACL(project, "suryagaddipati", false);
        GithubAuthenticationToken githubAuthentication = getGithubAuthentication("chairman-meow");
        Mockito.when(githubAuthentication.hasRepositoryPermission("groupon/DotCi")).thenReturn(true);
        Assert.assertTrue(acl.hasPermission(githubAuthentication, Permission.DELETE));

    }



    private GithubAuthenticationToken getGithubAuthentication(String user) {
        GithubAuthenticationToken auth = Mockito.mock(GithubAuthenticationToken.class);
        Mockito.when(auth.getName()).thenReturn(user);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);
        return auth;
    }
}