package com.groupon.jenkins.github;

import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.dynamic.build.cause.GithubLogEntry;

import java.util.List;

public abstract  class WebhookPayload {
    public abstract String getSha();

    public abstract String getCommitMessage();

    public abstract String getCommitterName();

    public abstract List<GithubLogEntry> getLogEntries();

    public abstract String getCommitterEmail();

    public abstract String getAvatarUrl();

    public abstract String getDiffUrl();

    public abstract String getBranch();

    public static WebhookPayload get(String eventType, String payloadData) {
        return  new PushAndPullRequestPayload(payloadData);
    }

    public abstract String getPusher();

    public abstract String getProjectUrl();

    public abstract boolean needsBuild(boolean b);

    public abstract BuildCause getCause();
}
