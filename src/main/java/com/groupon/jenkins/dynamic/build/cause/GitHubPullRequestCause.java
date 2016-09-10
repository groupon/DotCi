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
package com.groupon.jenkins.dynamic.build.cause;

import com.groupon.jenkins.github.PushAndPullRequestPayload;

import java.util.HashMap;
import java.util.Map;

public class GitHubPullRequestCause extends GithubPushPullWebhookCause {
    private final String label;
    private final String number;
    private final String targetBranch;
    private final String sourceBranch;

    public GitHubPullRequestCause(PushAndPullRequestPayload payload, String sha, String label, String number, String sourceBranch, String targetBranch) {
        super(payload, sha);
        this.label = label;
        this.number = number;
        this.targetBranch = targetBranch;
        this.sourceBranch = sourceBranch;

    }

    @Override
    public Map<String, String> getCauseEnvVars() {
        Map vars = new HashMap<>() ;
        putIfNotNull(vars, "DOTCI_PULL_REQUEST_LABEL", getLabel());
        putIfNotNull(vars, "DOTCI_PULL_REQUEST_TARGET_BRANCH", getTargetBranch());
        putIfNotNull(vars, "DOTCI_PULL_REQUEST_SOURCE_BRANCH", getSourceBranch());
        return vars;
    }

    @Override
    public String getShortDescription() {
        return "Started by Github pull request  " + label + "( Number : " + number + ")";
    }

    @Override
    public String getName() {
        return "GITHUB_PULL_REQUEST";
    }

    public String getLabel() {
        return label;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }
}
