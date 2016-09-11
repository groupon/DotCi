package com.groupon.jenkins.github;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.dynamic.build.cause.IssueCommentBuildCause;
import com.groupon.jenkins.git.GitBranch;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;

import java.io.IOException;
import java.io.StringReader;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

public class IssueCommentPayload implements WebhookPayload {

    static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setVisibilityChecker(new VisibilityChecker.Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private GHIssueComment comment;
    private GHIssue issue;
    private GHUser sender;
    private GHRepository repository;

    public static WebhookPayload get(final String payload) {
        try {
            return MAPPER.readValue(new StringReader(payload), IssueCommentPayload.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProjectUrl() {
        return this.repository.getHtmlUrl().toExternalForm();
    }

    @Override
    public boolean needsBuild(final DynamicProject b) {
        return true;
    }

    @Override
    public BuildCause getCause() {
        return new IssueCommentBuildCause(this);
    }

    @Override
    public String getBranch() {
        return this.issue.isPullRequest() ? GitBranch.PULL_REQUEST_PREFIX + this.issue.getNumber() : this.repository.getDefaultBranch();
    }

    public String getSha() {
        try {
            return new GithubRepositoryService(getProjectUrl()).getHeadCommitForBranch(getBranch()).getSHA1();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPusher() {
        return this.sender.getLogin();
    }

    public String getDescription() {
        return StringUtils.abbreviate(this.comment.getBody(), 40);
    }

    public String getPullRequestNumber() {
        return this.issue.isPullRequest() ? this.issue.getNumber() + "" : null;
    }
}
