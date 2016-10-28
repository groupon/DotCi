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
package com.groupon.jenkins.git;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitUrl {
    private static final Pattern GITHUB_HTTP_URL = Pattern.compile("^https?://(.*)/(.*)/(.*)");
    private static final Pattern GITHUB_SSH_URL = Pattern.compile("^git@(.*):(.*)/(.*).git");
    private final String url;
    private final String orgName;
    private final String name;
    private final String domain;

    public GitUrl(final String url) {
        final Matcher matcher = GITHUB_HTTP_URL.matcher(url);
        if (matcher.matches()) {
            this.orgName = matcher.group(2);
            this.name = matcher.group(3);
            this.domain = matcher.group(1);
            this.url = "git@" + this.domain + ":" + this.orgName + "/" + this.name + ".git";
        } else {
            this.url = url;
            final Matcher sshMatcher = GITHUB_SSH_URL.matcher(url);
            if (sshMatcher.matches()) {
                this.orgName = sshMatcher.group(2);
                this.name = sshMatcher.group(3);
                this.domain = sshMatcher.group(1);
            } else {
                throw new IllegalArgumentException("Invalid git url " + url);
            }

        }
    }

    public String getFullRepoName() {
        return this.orgName + "/" + this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public String getGitUrl() {
        return String.format("git@%s:%s/%s.git", this.domain, this.orgName, this.name);
    }


    public String getName() {
        return this.name;
    }

    public String getHttpsUrl() {
        return String.format("https://%s/%s/%s", this.domain, this.orgName, this.name);
    }

    public String applyTemplate(final String template) {
        return template.replace("<DOMAIN>", this.domain).replace("<ORG>", this.orgName).replace("<REPO>", this.name);
    }
}
