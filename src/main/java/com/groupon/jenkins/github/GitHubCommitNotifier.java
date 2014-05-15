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

import hudson.Extension;
import hudson.model.BuildListener;
import hudson.model.Result;

import java.io.IOException;

import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import com.groupon.jenkins.notifications.PostBuildNotifier;

import static hudson.model.Result.SUCCESS;
import static hudson.model.Result.UNSTABLE;

@Extension
public class GitHubCommitNotifier extends PostBuildNotifier {

	public GitHubCommitNotifier() {
		super("github_commit_status");
	}

	@Override
	protected Type getType() {
		return PostBuildNotifier.Type.ALL;
	}

	@Override
	protected boolean notify(DynamicBuild build, BuildListener listener) {
		String sha1 = build.getSha();
		if (sha1 == null) {
			return false;
		}

		GHRepository repository = getGithubRepository(build);
		GHCommitState state;
		String msg;
		Result result = build.getResult();
		if (result.isBetterOrEqualTo(SUCCESS)) {
			state = GHCommitState.SUCCESS;
			msg = "Success";
		} else if (result.isBetterOrEqualTo(UNSTABLE)) {
			state = GHCommitState.FAILURE;
			msg = "Unstable";
		} else {
			state = GHCommitState.FAILURE;
			msg = "Failed";
		}
		try {
			repository.createCommitStatus(sha1, state, build.getFullUrl(), msg);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		listener.getLogger().println("setting commit status on Github for " + repository.getUrl() + "/commit/" + sha1);

		return true;
	}

	protected GHRepository getGithubRepository(DynamicBuild build) {
		return new GithubRepositoryService(build.getGithubRepoUrl()).getGithubRepository();
	}

}