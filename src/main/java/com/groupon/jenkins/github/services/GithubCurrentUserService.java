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
package com.groupon.jenkins.github.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class GithubCurrentUserService {

    private final GHMyself user;
    private final GitHub gh;

    public GithubCurrentUserService(GitHub gh) {
        this.gh = gh;
        try {
            this.user = gh.getMyself();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Map<String, GHRepository> getRepositories(String orgName) {
        try {
            if (orgName.equals(user.getLogin())) {
                return user.getRepositories();
            } else {
                return gh.getOrganization(orgName).getRepositories();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurrentLogin() {
        return user.getLogin();
    }

    public Iterable<String> getOrgs() {
        try {
            List<String> allOrgs = new ArrayList<String>();
            allOrgs.add(getCurrentLogin());
            allOrgs.addAll(gh.getMyOrganizations().keySet());
            return allOrgs;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
