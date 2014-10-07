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
package com.groupon.jenkins.dynamic.build.cause;

import com.groupon.jenkins.github.GitBranch;
import com.groupon.jenkins.github.Payload;

import java.util.List;

public class GithubCause extends BuildCause {
	private final String sha;
	private final String pullRequestNumber;
	private final String pusher;
	private final String buildDescription;
	@SuppressWarnings("unused")
	private transient Payload payload; // For backward compatibility
	private final GitBranch branch;
	private final List<GithubLogEntry> logEntries;

	public GithubCause(Payload payload, String sha) {
		this.buildDescription = payload.getBuildDescription();
		this.pusher = payload.getPusher();
		this.pullRequestNumber = payload.isPullRequest() ? payload.getPullRequestNumber() : null;
		this.sha = sha;
		this.branch = new GitBranch(payload.getBranch());
		this.logEntries = payload.getLogEntries();
	}

	@Override
	public String getSha() {
		return sha;
	}

	@Override
	public String getShortDescription() {
		return "Started by Github push";
	}

	@Override
	public String getBuildDescription() {
		return buildDescription;
	}

	@Override
	public String getPusher() {
		return pusher;
	}

	@Override
	public String getPullRequestNumber() {
		return pullRequestNumber;
	}

	@Override
	public GitBranch getBranch() {
		return branch;
	}

	@Override
	public List<GithubLogEntry> getChangeLogEntries() {
		return logEntries;
	}
}
