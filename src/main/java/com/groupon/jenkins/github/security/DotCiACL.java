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
import com.groupon.jenkins.dynamic.build.DynamicSubProject;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import com.groupon.jenkins.github.GitSshUrl;
import hudson.model.AbstractItem;
import hudson.model.Job;
import hudson.security.ACL;
import hudson.security.Permission;
import org.acegisecurity.Authentication;
import org.jenkinsci.plugins.GithubAuthenticationToken;

public class DotCiACL extends ACL {
    private DynamicProject project;
    private OrganizationContainer container;
    private final String adminUserNames;
    private final boolean authenticatedUserReadPermission;

    public DotCiACL(Job<?, ?> job,String adminUserNames, boolean authenticatedUserReadPermission) {
        this(adminUserNames,authenticatedUserReadPermission);
        if (job instanceof DynamicProject) {
            this.project = (DynamicProject) job;
        }
        if (job instanceof DynamicSubProject) {
            this.project = ((DynamicSubProject) job).getParent();
        }
    }

    public DotCiACL(String adminUserNames, boolean authenticatedUserReadPermission) {
        this.adminUserNames = adminUserNames;
        this.authenticatedUserReadPermission = authenticatedUserReadPermission;
    }

    public DotCiACL(AbstractItem item,String adminUserNames, boolean authenticatedUserReadPermission) {
        this(adminUserNames, authenticatedUserReadPermission);
        if (item instanceof OrganizationContainer) {
            this.container = (OrganizationContainer) item;
        }

    }


    @Override
    public boolean hasPermission(Authentication a, Permission permission) {
        if (isSystem(a)|| isAdmin(a)) {
            return true;
        }
        if (isDotCi()) {
            if (a != null && a instanceof GithubAuthenticationToken && a.isAuthenticated()) {
                if (isReadPermission(permission) && authenticatedUserReadPermission) { //TODO: Block reads for Private Repos
                    return true;
                } else {
                    return isMember((GithubAuthenticationToken) a);
                }

            }
            return false;
        }
        return  isReadPermission(permission);
    }

    private boolean isSystem(Authentication a) {
        return a == ACL.SYSTEM;
    }


    private boolean isAdmin(Authentication a) {
        return a != null && adminUserNames !=null && adminUserNames.contains(a.getName());
    }



    private boolean isMember(GithubAuthenticationToken a) {
        if (container != null) {
            return isOrgMember(a);
        }
        return isRepoMember(a);
    }

    private boolean isRepoMember(GithubAuthenticationToken authentication) {
        String repoName = new GitSshUrl(project.getGithubRepoUrl()).getFullRepoName();
        return authentication.hasRepositoryPermission(repoName);
    }

    private boolean isOrgMember(GithubAuthenticationToken authentication) {
        String candidateName = authentication.getName();
        return  candidateName.equals(container.getName()) || authentication.hasOrganizationPermission(candidateName, container.getName());
    }

    private boolean isDotCi() {
        return container != null || project != null;
    }


    private boolean isReadPermission(Permission permission) {
        if (permission.getId().equals("hudson.model.Hudson.Read")
                || permission.getId().equals("hudson.model.Item.Workspace")
                || permission.getId().equals("hudson.model.Item.Read")) {
            return true;
        } else
            return false;
    }
}
