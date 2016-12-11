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
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommitStatus;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.PagedIterable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommitHistoryViewTest {

    @Test
    public void should_group_commit_statueses_by_context() throws IOException {
        final CommitHistoryView commitHistoryView = new CommitHistoryView();
        final DynamicBuild build = mock(DynamicBuild.class);
        final GHRepository githubRepo = mock(GHRepository.class);
        when(build.getGithubRepository()).thenReturn(githubRepo);

        final PagedIterable<GHCommitStatus> commitStatusesList = mock(PagedIterable.class);
        final GHCommitStatus commitStatus1 = mock(GHCommitStatus.class);
        final GHCommitStatus commitStatus2 = mock(GHCommitStatus.class);

        when(commitStatus1.getContext()).thenReturn("Default");
        when(commitStatus2.getContext()).thenReturn("DotCi");

        when(commitStatusesList.asList()).thenReturn(Arrays.asList(commitStatus1, commitStatus2));
        final GHCommit currentCommit = mock(GHCommit.class);
        when(githubRepo.getCommit(null)).thenReturn(currentCommit);
        when(currentCommit.listStatuses()).thenReturn(commitStatusesList);
        when(githubRepo.getLastCommitStatus(null)).thenReturn(commitStatus1);
        commitHistoryView.onLoad(build);
        final Map<String, List<GHCommitStatus>> commitStatuses = commitHistoryView.getCommitStatuses();

        Assert.assertEquals(commitStatuses.get("Default").get(0), commitStatus1);
        Assert.assertEquals(commitStatuses.get("DotCi").get(0), commitStatus2);
    }
}
