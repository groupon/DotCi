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
package com.groupon.jenkins.github;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.dynamic.build.cause.ManualBuildCause;
import com.groupon.jenkins.git.GitBranch;
import com.groupon.jenkins.testhelpers.BuildListenerFactory;
import hudson.model.Action;
import hudson.model.CauseAction;
import hudson.model.Queue;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static com.groupon.jenkins.testhelpers.DynamicBuildFactory.newBuild;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommitStatusUpdateQueueListenerTest {

    @Test
    public void should_set_building_status_on_queue_enter() throws IOException {
        final GHRepository githubRepository = mock(GHRepository.class);
        CommitStatusUpdateQueueListener commitStatusUpdateQueueListener = new CommitStatusUpdateQueueListener() {
            @Override
            protected GHRepository getGithubRepository(DynamicProject project) {
                return githubRepository;
            }

            @Override
            protected String getJenkinsRootUrl() {
                return "jenkins.root.url" ;
            }
        };

        BuildCause buildCause = mock(BuildCause.class);

        ArrayList<Action> causeActions = new ArrayList<Action>();
        causeActions.add(new CauseAction(buildCause));
        Queue.WaitingItem queueItem = new Queue.WaitingItem(null,null, causeActions);


        when(buildCause.getSha()).thenReturn("sha");

        commitStatusUpdateQueueListener.onEnterWaiting(queueItem);

        verify(githubRepository).createCommitStatus("sha", GHCommitState.PENDING, "jenkins.root.url", "Build in queue.","DotCi/push");
    }



}
