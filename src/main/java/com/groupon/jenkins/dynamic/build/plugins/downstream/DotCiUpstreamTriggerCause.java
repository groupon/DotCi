/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.groupon.jenkins.dynamic.build.plugins.downstream;

import com.google.common.collect.Lists;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.dynamic.build.cause.GithubLogEntry;
import com.groupon.jenkins.git.GitBranch;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import org.kohsuke.github.GHCommit;

import java.io.IOException;
import java.util.Map;

public class DotCiUpstreamTriggerCause extends BuildCause {

    private final String pusher;
    private final String description;
    private final CommitInfo commitInfo;
    private final String sha;
    private final String parentSha;
    private GitBranch branch;

    public DotCiUpstreamTriggerCause(DynamicBuild sourceBuild, DynamicProject targetJob, Map<String, String> jobOptions) {
        branch = new GitBranch(jobOptions.get("BRANCH"));
        GHCommit commit;
        if (jobOptions.containsKey("SHA")) {
            commit = getCommitFromSha(targetJob, jobOptions.get("SHA"));
        } else {
            commit = getHeadCommitForBranch(targetJob, branch.toString());
        }
        this.pusher = sourceBuild.getCause().getPusher();
        this.description = "Started by upstream job  " + sourceBuild.getParent().getFullDisplayName() + ".: " + sourceBuild.getNumber();
        this.sha = commit.getSHA1();
        this.parentSha = commit.getParentSHA1s().size() > 0 ? commit.getParentSHA1s().get(0) : null;
        this.commitInfo = new CommitInfo(commit, branch);
    }

    @Override
    public String getSha() {
        return sha;
    }

    @Override
    public String getParentSha() {
        return parentSha;
    }

    private GHCommit getHeadCommitForBranch(DynamicProject targetJob, String branch) {
        try {
            return new GithubRepositoryService(targetJob.getGithubRepoUrl()).getHeadCommitForBranch(branch);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPusher() {
        return pusher;
    }

    @Override
    public String getPullRequestNumber() {
        return null;
    }

    @Override
    public CommitInfo getCommitInfo() {
        return this.commitInfo;
    }

    @Override
    public String getName() {
        return "UPSTREAM";
    }

    private GHCommit getCommitFromSha(DynamicProject targetJob, String sha) {
        try {
            return new GithubRepositoryService(targetJob.getGithubRepoUrl()).getGithubRepository().getCommit(sha);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GitBranch getBranch() {
        return branch;
    }

    @Override
    public Iterable<GithubLogEntry> getChangeLogEntries() {
        return Lists.newArrayList();
    }

    @Override
    public String getShortDescription() {
        return this.description;
    }
}
