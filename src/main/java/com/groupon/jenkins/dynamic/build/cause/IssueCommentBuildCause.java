package com.groupon.jenkins.dynamic.build.cause;

import com.groupon.jenkins.git.GitBranch;
import com.groupon.jenkins.github.IssueCommentPayload;
import org.kohsuke.github.GHEvent;

import java.util.ArrayList;

public class IssueCommentBuildCause extends BuildCause {
    private final String sha;
    private final GitBranch branch;
    private final String pusher;
    private final String shortDescription;
    private final CommitInfo commitInfo;
    private final String pullRequestNumber;

    public IssueCommentBuildCause(final IssueCommentPayload issueCommentPayload) {
        this.sha = issueCommentPayload.getSha();
        this.pusher = issueCommentPayload.getPusher();
        this.branch = new GitBranch(issueCommentPayload.getBranch());
        this.shortDescription = issueCommentPayload.getDescription();
        this.pullRequestNumber = issueCommentPayload.getPullRequestNumber();
        this.commitInfo = new CommitInfo(issueCommentPayload.getDescription(), issueCommentPayload.getPusher(), issueCommentPayload.getBranch());

    }

    @Override
    public String getSha() {
        return this.sha;
    }

    @Override
    public String getParentSha() {
        return null;
    }

    @Override
    public String getPusher() {
        return this.pusher;
    }

    @Override
    public String getPullRequestNumber() {
        return this.pullRequestNumber;
    }

    @Override
    public CommitInfo getCommitInfo() {
        return this.commitInfo;
    }

    @Override
    public String getName() {
        return GHEvent.ISSUE_COMMENT.toString();
    }

    @Override
    public GitBranch getBranch() {
        return this.branch;
    }

    @Override
    public Iterable<GithubLogEntry> getChangeLogEntries() {
        return new ArrayList<>();
    }

    @Override
    public String getShortDescription() {
        return this.shortDescription;
    }
}
