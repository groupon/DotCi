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

import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.dynamic.build.cause.GitHubPullRequestCause;
import com.groupon.jenkins.dynamic.build.cause.GitHubPushCause;
import com.groupon.jenkins.dynamic.build.cause.GithubLogEntry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PushAndPullRequestPayload implements WebhookPayload {


    private final JSONObject payloadJson;

    public PushAndPullRequestPayload(final String payload) {
        this.payloadJson = JSONObject.fromObject(payload);
    }

    public boolean isPullRequest() {
        return this.payloadJson.containsKey("pull_request");
    }

    public BuildCause getCause() {
        if (isPullRequest()) {
            final JSONObject pullRequest = getPullRequest();
            final String label = pullRequest.getJSONObject("head").getString("label");
            final String number = pullRequest.getString("number");
            return new GitHubPullRequestCause(this, getSha(), label, number, getPullRequestSourceBranch(), getPullRequestTargetBranch());
        } else {
            final String pusherName = this.payloadJson.getJSONObject("pusher").getString("name");
            final String email = this.payloadJson.getJSONObject("pusher").getString("email");
            return new GitHubPushCause(this, getSha(), pusherName, email);
        }
    }

    public String getSha() {
        if (isPullRequest()) {
            return getPullRequest().getJSONObject("head").getString("sha");
        }
        return this.payloadJson.getJSONObject("head_commit").getString("id");
    }

    private JSONObject getPullRequest() {
        return this.payloadJson.getJSONObject("pull_request");
    }

    public String getBranch() {
        if (isPullRequest()) {
            return "Pull Request: " + getPullRequestNumber();
        }
        return this.payloadJson.getString("ref").replaceAll("refs/", "").replaceAll("heads/", "");
    }

    public boolean needsBuild(final DynamicProject project) {
        if (this.payloadJson.has("ref") && this.payloadJson.getString("ref").startsWith("refs/tags/")) {
            return project.shouldBuildTags();
        }
        if (isPullRequest()) {
            return shouldBuildPullRequest();
        }
        return !this.payloadJson.getBoolean("deleted");
    }

    private boolean shouldBuildPullRequest() {
        return !isPullRequestClosed() &&
            shouldBuildPullRequestBasedOnAction();
    }

    //only build for webhook actions of "opened", "reopened", or "synchronize"
    //https://developer.github.com/v3/activity/events/types/#events-api-payload-17
    private boolean shouldBuildPullRequestBasedOnAction() {
        return isOpenedAction() || isReOpenedAction() || isSynchronizeAction();
    }

    private boolean isPullRequestClosed() {
        return "closed".equals(getPullRequest().getString("state"));
    }

    private boolean isOpenedAction() {
        return isPullRequest() && "opened".equals(this.payloadJson.getString("action"));
    }

    private boolean isReOpenedAction() {
        return isPullRequest() && "reopened".equals(this.payloadJson.getString("action"));
    }

    private boolean isSynchronizeAction() {
        return isPullRequest() && "synchronize".equals(this.payloadJson.getString("action"));
    }

    public String getPullRequestSourceBranch() {
        if (!isPullRequest()) {
            return null;
        }
        return getPullRequest().getJSONObject("head").getString("ref");
    }

    public String getPullRequestTargetBranch() {
        if (!isPullRequest()) {
            return null;
        }
        return getPullRequest().getJSONObject("base").getString("ref");
    }

    public String getBranchDescription() {
        if (isPullRequest()) {
            return "Pull Request " + getPullRequestNumber();
        }
        return getBranch().replace("origin/", "");
    }

    public String getPullRequestNumber() {
        return getPullRequest().getString("number");
    }

    public String getPusher() {
        if (isPullRequest()) {
            return this.payloadJson.getJSONObject("sender").getString("login");
        }
        return this.payloadJson.getJSONObject("pusher").getString("name");
    }

    public String getDiffUrl() {
        if (isPullRequest()) {
            return getPullRequest().getString("html_url");
        } else {
            return this.payloadJson.getString("compare");
        }
    }

    public String getProjectUrl() {
        if (isPullRequest()) {
            return getPullRequest().getJSONObject("base").getJSONObject("repo").getString("html_url");
        }
        return this.payloadJson.getJSONObject("repository").getString("url");
    }

    public List<GithubLogEntry> getLogEntries() {
        final List<GithubLogEntry> logEntries = new ArrayList<>();
        if (!isPullRequest()) {
            final JSONArray commits = this.payloadJson.getJSONArray("commits");
            for (final Object commit : commits) {
                logEntries.add(convertToLogEntry((Map<String, Object>) commit));
            }
        }
        return logEntries;
    }

    private GithubLogEntry convertToLogEntry(final Map<String, Object> commit) {
        final List<String> affectedPaths = new ArrayList<>();
        affectedPaths.addAll((java.util.Collection<? extends String>) commit.get("added"));
        affectedPaths.addAll((java.util.Collection<? extends String>) commit.get("modified"));
        affectedPaths.addAll((java.util.Collection<? extends String>) commit.get("removed"));
        return new GithubLogEntry(commit.get("message").toString(), commit.get("url").toString(), commit.get("id").toString(), affectedPaths);
    }

    public String getCommitterName() {
        if (isPullRequest()) {
            return this.payloadJson.getJSONObject("sender").getString("login");
        }
        return this.payloadJson.getJSONObject("head_commit").getJSONObject("committer").getString("name");
    }

    public String getCommitterEmail() {
        if (isPullRequest()) {
            return null;
        }
        return this.payloadJson.getJSONObject("head_commit").getJSONObject("committer").getString("email");
    }

    public String getAvatarUrl() {
        if (isPullRequest()) {
            return this.payloadJson.getJSONObject("sender").getString("avatar_url");
        }
        return null;
    }

    public String getCommitMessage() {
        if (isPullRequest()) {
            return this.payloadJson.getJSONObject("pull_request").getString("title");
        }
        return this.payloadJson.getJSONObject("head_commit").getString("message");
    }

    public String getParentSha() {
        if (isPullRequest()) {
            return this.payloadJson.getJSONObject("pull_request").getJSONObject("base").getString("sha");
        }
        return this.payloadJson.getString("before");
    }
}
