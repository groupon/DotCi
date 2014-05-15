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

import java.io.IOException;

import org.junit.Test;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;

import com.groupon.jenkins.dynamic.build.DynamicBuild;

import static com.groupon.jenkins.testhelpers.DynamicBuildFactory.newBuild;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CommitStatusUpdateListenerTest {

	@Test
	public void should_set_building_status_on_commit() throws IOException {
		final GHRepository repository = mock(GHRepository.class);
		CommitStatusUpdateListener commitStatusUpdateListener = new CommitStatusUpdateListener() {
			@Override
			protected GHRepository getGithubRepository(DynamicBuild build) {
				return repository;
			}
		};

		DynamicBuild build = newBuild().get();
		commitStatusUpdateListener.onStarted(build, null);

		verify(repository).createCommitStatus(build.getSha(), GHCommitState.PENDING, build.getFullUrl(), "Build in progress");
	}
}
