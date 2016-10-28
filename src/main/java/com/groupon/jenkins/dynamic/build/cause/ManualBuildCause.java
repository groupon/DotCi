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

import com.google.common.base.Objects;
import com.groupon.jenkins.git.GitBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ManualBuildCause extends BuildCause {

    private final String user;
    private final GitBranch branch;
    private final String parentSha;
    private final String sha;
    private final CommitInfo commitInfo;
    private final List<GithubLogEntry> changeLogEntries;

    public ManualBuildCause(final GitBranch branch, final GHCommit commit, final String parentSha, final String user) throws IOException {
        this.branch = branch;
        this.parentSha = parentSha;
        this.sha = commit.getSHA1();
        this.user = user;
        this.commitInfo = new CommitInfo(commit, branch);
        this.changeLogEntries = getChangeLogEntriescommit(commit);
    }

    private List<GithubLogEntry> getChangeLogEntriescommit(final GHCommit commit) throws IOException {
        final List<GithubLogEntry> logEntries = new ArrayList<>();
        logEntries.add(new GithubLogEntry(commit));
        return logEntries;
    }


    @Override
    @Exported(visibility = 3)
    public String getShortDescription() {
        return "Started by: " + this.user;
    }

    @Override
    public String getSha() {
        return this.sha;
    }

    @Override
    public String getParentSha() {
        return this.parentSha;
    }


    @Override
    public String getPusher() {
        return this.user;
    }

    @Override
    public String getPullRequestNumber() {
        return this.branch.isPullRequest() ? this.branch.pullRequestNumber() + "" : null;
    }

    @Override
    public CommitInfo getCommitInfo() {
        return this.commitInfo;
    }

    @Override
    public String getName() {
        return "MANUAL";
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.branch, this.sha, this.user);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ManualBuildCause other = (ManualBuildCause) obj;
        return Objects.equal(this.branch, other.branch) && Objects.equal(this.sha, other.sha) && Objects.equal(this.user, other.user);
    }

    @Override
    public GitBranch getBranch() {
        return this.branch;
    }

    @Override
    public Iterable<GithubLogEntry> getChangeLogEntries() {
        //null check here for backward compat with builds that were built without this field.
        return this.changeLogEntries == null ? new ArrayList<>() : this.changeLogEntries;
    }
}
