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

import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.security.ACL;
import hudson.security.AuthorizationStrategy;
import java.util.ArrayList;
import java.util.Collection;
import org.kohsuke.stapler.DataBoundConstructor;

public class DotCiAuthorizationStategy extends AuthorizationStrategy {
    private String adminUserNames;
    private boolean authenticatedUserReadPermission;

    @DataBoundConstructor
    public DotCiAuthorizationStategy(String adminUserNames,boolean authenticatedUserReadPermission ){
        this.adminUserNames = adminUserNames;
        this.authenticatedUserReadPermission = authenticatedUserReadPermission;
    }
    @Override
    public ACL getRootACL() {
        return new DotCiACL(adminUserNames,authenticatedUserReadPermission) ;
    }
    public ACL getACL(Job<?,?> job) {
        return new DotCiACL(job,adminUserNames,authenticatedUserReadPermission) ;
    }
    public ACL getACL(AbstractItem item) {
       return new DotCiACL(item,adminUserNames,authenticatedUserReadPermission) ;
    }

    public String getAdminUserNames() {
        return adminUserNames;
    }

    public boolean isAuthenticatedUserReadPermission() {
        return authenticatedUserReadPermission;
    }

    @Override
    public Collection<String> getGroups() {
        return new ArrayList<String>();
    }
    @Extension
    public static final class DescriptorImpl extends
            Descriptor<AuthorizationStrategy> {

        public String getDisplayName() {
            return "DotCi Authorization";
        }

    }
}
