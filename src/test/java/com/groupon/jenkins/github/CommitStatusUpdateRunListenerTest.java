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
import com.groupon.jenkins.testhelpers.BuildListenerFactory;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;

import java.io.IOException;

import static com.groupon.jenkins.testhelpers.DynamicBuildFactory.newBuild;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CommitStatusUpdateRunListenerTest {

    private GHRepository githubRepository;
    private CommitStatusUpdateRunListener commitStatusUpdateRunListener;

    @Before
    public void setupTaget() {
        this.githubRepository = mock(GHRepository.class);
        this.commitStatusUpdateRunListener = new CommitStatusUpdateRunListener() {
            @Override
            protected GHRepository getGithubRepository(final DynamicBuild build) {
                return CommitStatusUpdateRunListenerTest.this.githubRepository;
            }
        };

    }

    @Test
    public void should_set_building_status_on_commit() throws IOException {
        final DynamicBuild build = newBuild().get();
        this.commitStatusUpdateRunListener.onInitialize(build);

        verify(this.githubRepository).createCommitStatus(build.getSha(), GHCommitState.PENDING, build.getFullUrl(), "Build in progress", "DotCi/push");
    }

    @Test
    public void should_set_success_status_on_commit_if_build_is_successful() throws IOException {
        final DynamicBuild build = newBuild().success().get();

        this.commitStatusUpdateRunListener.onCompleted(build, BuildListenerFactory.newBuildListener().get());
        verify(this.githubRepository).createCommitStatus(build.getSha(), GHCommitState.SUCCESS, build.getFullUrl(), "Success", "DotCi/push");


    }

    @Test
    public void should_set_skipped_message_on_skipped_build() throws IOException {
        final DynamicBuild build = newBuild().success().skipped().get();

        this.commitStatusUpdateRunListener.onCompleted(build, BuildListenerFactory.newBuildListener().get());
        verify(this.githubRepository).createCommitStatus(build.getSha(), GHCommitState.SUCCESS, build.getFullUrl(), "Success - Skipped", "DotCi/push");


    }

    @Test
    public void should_set_failure_status_on_commit_if_build_fails() throws IOException {
        final DynamicBuild build = newBuild().fail().get();
        this.commitStatusUpdateRunListener.onCompleted(build, BuildListenerFactory.newBuildListener().get());
        verify(this.githubRepository).createCommitStatus(build.getSha(), GHCommitState.FAILURE, build.getFullUrl(), "Failed", "DotCi/push");
    }

    @Test
    public void should_set_failure_status_on_commit_if_build_is_unstable() throws IOException {
        final DynamicBuild build = newBuild().unstable().get();
        this.commitStatusUpdateRunListener.onCompleted(build, BuildListenerFactory.newBuildListener().get());
        verify(this.githubRepository).createCommitStatus(build.getSha(), GHCommitState.FAILURE, build.getFullUrl(), "Unstable", "DotCi/push");
    }


}
