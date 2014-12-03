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
import com.groupon.jenkins.github.GitBranch;
import com.groupon.jenkins.github.GitUrl;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import hudson.model.Cause.UserIdCause;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Testable proxy of dyanamicbuild
public class DynamicBuildModel {

    private final DynamicBuild build;
    private final GithubRepositoryService githubRepositoryService;

    public DynamicBuildModel(DynamicBuild build) {
        this(build, new GithubRepositoryService(build.getGithubRepoUrl()));
    }

    protected DynamicBuildModel(DynamicBuild build, GithubRepositoryService githubRepositoryService) {
        this.build = build;
        this.githubRepositoryService = githubRepositoryService;
    }



    public void run() {
        addBuildCauseForNonGithubCauses();
        this.build.addAction(new CommitHistoryView());
    }

    private void addBuildCauseForNonGithubCauses() {
        if (build.getCause(UserIdCause.class) != null) {
            String user = build.getCause(UserIdCause.class).getUserId();
            String branch = build.getEnvVars().get("BRANCH");
            String sha = githubRepositoryService.getShaForBranch(branch);
            ManualBuildCause manualCause = new ManualBuildCause(new GitBranch(branch), sha, user);
            build.addCause(manualCause);
        }
        if (build.getCause() == BuildCause.NULL_BUILD_CAUSE) {
            String branch = build.getEnvVars().get("BRANCH");
            String sha = githubRepositoryService.getShaForBranch(branch);
            build.addCause(new UnknownBuildCause(new GitBranch(branch), sha));
        }
    }

    public void deleteBuild() throws IOException {
        List<DynamicSubBuild> runs = getExactRuns();
        for (DynamicSubBuild run : runs) {
            run.delete();
        }
        build.delete();
    }

    private List<DynamicSubBuild> getExactRuns() {
        List<DynamicSubBuild> r = new ArrayList<DynamicSubBuild>();
        for (DynamicSubProject c : build.getParent().getItems()) {
            DynamicSubBuild b = c.getBuildByNumber(build.getNumber());
            if (b != null) {
                r.add(b);
            }
        }
        return r;
    }

    public BuildCause getBuildCause() {
        BuildCause buildCause = build.getCause(BuildCause.class);
        return buildCause == null ? BuildCause.NULL_BUILD_CAUSE : buildCause;
    }

    public Map<String, String> getDotCiEnvVars() {
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("SHA", build.getSha());
        vars.put("GIT_URL", new GitUrl(build.getParent().getGithubRepoUrl()).getUrl());
        vars.put("DOTCI_IS_PRIVATE_REPO", new Boolean(build.isPrivateRepo()).toString()) ;
        return vars;
    }

    public Map<String, Object> getEnvironmentWithChangeSet(TaskListener listener) throws IOException, InterruptedException {
        HashMap<String, Object> environmentWithChangeSet = new HashMap<String, Object>();
        environmentWithChangeSet.putAll(build.getEnvironment(listener));
        environmentWithChangeSet.put("DOTCI_CHANGE_SET",getChangeSet());
        return environmentWithChangeSet;
    }

    private List<String> getChangeSet() {
        ArrayList<String> changeSet = new ArrayList<String>();
        for(ChangeLogSet.Entry change : build.getChangeSet()){
           changeSet.addAll(change.getAffectedPaths());
        }
        return changeSet;
    }
}
