package com.groupon.jenkins.dynamic.build.cause;

import java.util.ArrayList;

import org.kohsuke.stapler.export.Exported;

import com.groupon.jenkins.github.GitBranch;

public class NullBuildCause extends BuildCause {
	@Override
	@Exported(visibility = 3)
	public String getShortDescription() {
		return "";
	}

	@Override
	public String getSha() {
		return "unknown";
	}

	@Override
	public String getPusher() {
		return "unknown";
	}

	@Override
	public String getPullRequestNumber() {
		return "";
	}

	@Override
	public Iterable<GithubLogEntry> getChangeLogEntries() {
		return new ArrayList<GithubLogEntry>();
	}

	@Override
	public String getBuildDescription() {
		return "";
	}

	@Override
	public GitBranch getBranch() {
		return new GitBranch("undetermined");
	}
}
