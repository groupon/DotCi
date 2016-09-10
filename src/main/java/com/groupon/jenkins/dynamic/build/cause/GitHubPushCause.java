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

public class GitHubPushCause extends GithubPushPullWebhookCause {

    private final String pushedBy;
    private final String pusherEmailAddress;

    public GitHubPushCause(PushAndPullRequestPayload payload, String sha, String pusher, String pusherEmailAddress) {
        super(payload, sha);
        this.pushedBy = pusher;
        this.pusherEmailAddress = pusherEmailAddress;
    }

    @Override
    public String getShortDescription() {
        String pusher = getPushedBy() != null ? getPushedBy() : "<unknown>";
        return "Started by GitHub push by " + pusher;
    }

    public String getPusherEmailAddress() {
        return pusherEmailAddress;
    }

    public String getPushedBy() {
        return pushedBy;
    }

    @Override
    public String getName() {
        return "GITHUB_PUSH";
    }
}
