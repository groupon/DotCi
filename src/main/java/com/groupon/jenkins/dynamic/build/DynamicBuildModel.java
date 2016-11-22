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
package com.groupon.jenkins.dynamic.build;

import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.dynamic.build.cause.ManualBuildCause;
import com.groupon.jenkins.dynamic.build.cause.UnknownBuildCause;
import com.groupon.jenkins.dynamic.build.commithistory.CommitHistoryView;
import com.groupon.jenkins.git.GitBranch;
import com.groupon.jenkins.git.GitUrl;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import hudson.model.BuildListener;
import hudson.model.Cause.UserIdCause;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHPullRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Testable proxy of dyanamicbuild
public class DynamicBuildModel {

    private final DynamicBuild build;
    private final GithubRepositoryService githubRepositoryService;

    public DynamicBuildModel(final DynamicBuild build) {
        this(build, new GithubRepositoryService(build.getGithubRepoUrl()));
    }

    protected DynamicBuildModel(final DynamicBuild build, final GithubRepositoryService githubRepositoryService) {
        this.build = build;
        this.githubRepositoryService = githubRepositoryService;
    }


    public void run(final BuildListener listener) throws IOException, InterruptedException {
        addBuildCauseForNonGithubCauses(listener);
        this.build.addAction(new CommitHistoryView());
    }

    private void addBuildCauseForNonGithubCauses(final BuildListener listener) throws IOException, InterruptedException {
        final String branch = this.build.getEnvironment(listener).get("BRANCH");
        final GitBranch gitBranch = new GitBranch(branch);
        if (this.build.getCause(UserIdCause.class) != null) {
            final GHCommit commit = this.githubRepositoryService.getHeadCommitForBranch(branch);
            final String user = this.build.getCause(UserIdCause.class).getUserId();
            final ManualBuildCause manualCause = new ManualBuildCause(gitBranch, commit, getParentSha(commit, gitBranch), user);
            this.build.addCause(manualCause);
        }
        if (this.build.getCause() == BuildCause.NULL_BUILD_CAUSE) {
            final GHCommit commit = this.githubRepositoryService.getHeadCommitForBranch(branch);
            this.build.addCause(new UnknownBuildCause(gitBranch, commit, getParentSha(commit, gitBranch)));
        }
    }

    private String getParentSha(final GHCommit commit, final GitBranch gitBranch) throws IOException {
        final String parentSha;
        if (gitBranch.isPullRequest()) {
            final GHPullRequest pullRequest = this.githubRepositoryService.getGithubRepository().getPullRequest(gitBranch.pullRequestNumber());
            parentSha = pullRequest.getBase().getSha();

        } else {
            parentSha = (commit.getParentSHA1s() != null && commit.getParentSHA1s().size() > 0) ? commit.getParentSHA1s().get(0) : null;
        }
        return parentSha;
    }

    public void deleteBuild() throws IOException {
        deleteSubBuilds();
        this.build.delete();
    }

    public void deleteSubBuilds() throws IOException {
        final List<DynamicSubBuild> runs = getExactRuns();
        for (final DynamicSubBuild run : runs) {
            run.delete();
        }
    }

    private List<DynamicSubBuild> getExactRuns() {
        final List<DynamicSubBuild> r = new ArrayList<>();
        for (final DynamicSubProject c : this.build.getParent().getItems()) {
            final DynamicSubBuild b = c.getBuildByNumber(this.build.getNumber());
            if (b != null) {
                r.add(b);
            }
        }
        return r;
    }

    public BuildCause getBuildCause() {
        final BuildCause buildCause = this.build.getCause(BuildCause.class);
        return buildCause == null ? BuildCause.NULL_BUILD_CAUSE : buildCause;
    }

    public Map<String, String> getDotCiEnvVars() {
        final Map<String, String> vars = new HashMap<>();
        vars.put("SHA", this.build.getSha());
        if (this.build.isPrivateRepo()) {
            vars.put("GIT_URL", new GitUrl(this.build.getParent().getGithubRepoUrl()).getUrl());
            vars.put("DOTCI_IS_PRIVATE_REPO", "true");
        } else {
            vars.put("GIT_URL", this.build.getParent().getGithubRepoUrl());
            vars.put("DOTCI_IS_PRIVATE_REPO", "false");
        }
        return vars;
    }

    public Map<String, Object> getEnvironmentWithChangeSet(final TaskListener listener) throws IOException, InterruptedException {
        final HashMap<String, Object> environmentWithChangeSet = new HashMap<>();
        environmentWithChangeSet.putAll(this.build.getEnvironment(listener));
        environmentWithChangeSet.put("DOTCI_CHANGE_SET", getChangeSet());
        environmentWithChangeSet.put("build", this.build);
        return environmentWithChangeSet;
    }

    private List<String> getChangeSet() {
        final ArrayList<String> changeSet = new ArrayList<>();
        for (final ChangeLogSet.Entry change : this.build.getChangeSet()) {
            changeSet.addAll(change.getAffectedPaths());
        }
        return changeSet;
    }
}
