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

import com.groupon.jenkins.git.GitBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.stapler.export.Exported;

import java.util.Collections;


public class UnknownBuildCause extends BuildCause {

    private final String sha;
    private final GitBranch branch;
    private final String parentSha;
    private final CommitInfo commitInfo;

    public UnknownBuildCause(final GitBranch branch, final GHCommit commit, final String parentSha) {
        this.branch = branch;
        this.parentSha = parentSha;
        this.sha = commit.getSHA1();
        this.commitInfo = new CommitInfo(commit, branch);
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
        return "unknown";
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
        return "UNKNOWN";
    }

    @Override
    public GitBranch getBranch() {
        return this.branch;
    }

    @Override
    public Iterable<GithubLogEntry> getChangeLogEntries() {
        return Collections.emptyList();
    }

    @Override
    @Exported(visibility = 3)
    public String getShortDescription() {
        return "";
    }

}
