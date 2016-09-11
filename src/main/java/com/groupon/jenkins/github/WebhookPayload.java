package com.groupon.jenkins.github;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import org.kohsuke.github.GHEvent;

public interface WebhookPayload {


    static WebhookPayload get(final String eventType, final String payloadData) {
        switch (GHEvent.valueOf(eventType.toUpperCase())) {
            case PULL_REQUEST:
                return new PushAndPullRequestPayload(payloadData);
            case PUSH:
                return new PushAndPullRequestPayload(payloadData);
            case ISSUE_COMMENT:
                return IssueCommentPayload.get(payloadData);
            default:
                throw new RuntimeException("Event type: " + eventType + " not supported");
        }
    }

    String getProjectUrl();

    boolean needsBuild(DynamicProject project);

    BuildCause getCause();

    String getBranch();
}
