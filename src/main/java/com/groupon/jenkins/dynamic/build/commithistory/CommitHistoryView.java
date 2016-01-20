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

package com.groupon.jenkins.dynamic.build.commithistory;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import hudson.model.Run;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jenkins.model.RunAction2;
import org.kohsuke.github.GHCommitStatus;
import org.kohsuke.github.GHRepository;

public class CommitHistoryView implements RunAction2 {

    private transient  DynamicBuild build;

    @Override
    public String getIconFileName() {
        return "new-package.png";
    }

    @Override
    public String getDisplayName() {
        return "Commit History";
    }

    @Override
    public String getUrlName() {
        return "commitHistory";
    }

    @Override
    public void onAttached(Run<?, ?> r) {
        this.build = (DynamicBuild) r;
    }

    @Override
    public void onLoad(Run<?, ?> r) {
        this.build = (DynamicBuild) r;
    }

    public Map<String, List<GHCommitStatus>> getCommitStatuses() throws IOException {
        GHRepository githubRepository = build.getParent().getGithubRepository();
        List<GHCommitStatus> commitStatuses = githubRepository.getCommit(build.getSha()).listStatuses().asList();
        Map<String,List<GHCommitStatus>> groupedStatuses = new HashMap<String, List<GHCommitStatus>>();
        for(GHCommitStatus status : commitStatuses){
            String context = status.getContext();
            if(groupedStatuses.get(context) == null){
               groupedStatuses.put(context, new ArrayList<GHCommitStatus>());
            }
            groupedStatuses.get(context).add(status);
        }
        groupedStatuses.put("- Latest Status -", Arrays.asList(githubRepository.getLastCommitStatus(build.getSha())));
       return groupedStatuses;
    }
}
