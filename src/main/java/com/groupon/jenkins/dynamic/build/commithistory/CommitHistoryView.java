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
import jenkins.model.RunAction2;
import org.kohsuke.github.GHCommitStatus;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommitHistoryView implements RunAction2 {

    private transient DynamicBuild build;

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
    public void onAttached(final Run<?, ?> r) {
        this.build = (DynamicBuild) r;
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
        this.build = (DynamicBuild) r;
    }

    public Map<String, List<GHCommitStatus>> getCommitStatuses() throws IOException {
        final GHRepository githubRepository = this.build.getGithubRepository();
        final List<GHCommitStatus> commitStatuses = githubRepository.getCommit(this.build.getSha()).listStatuses().asList();
        final Map<String, List<GHCommitStatus>> groupedStatuses = new HashMap<>();
        for (final GHCommitStatus status : commitStatuses) {
            final String context = status.getContext();
            if (groupedStatuses.get(context) == null) {
                groupedStatuses.put(context, new ArrayList<>());
            }
            groupedStatuses.get(context).add(status);
        }
        groupedStatuses.put("- Latest Status -", Arrays.asList(githubRepository.getLastCommitStatus(this.build.getSha())));
        return groupedStatuses;
    }
}
