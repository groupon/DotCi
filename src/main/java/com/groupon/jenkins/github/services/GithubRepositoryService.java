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
package com.groupon.jenkins.github.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHDeployKey;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.github.GitBranch;
import com.groupon.jenkins.github.GitSshUrl;
import com.groupon.jenkins.github.GithubUtils;
import com.groupon.jenkins.mongo.GithubAccessTokenRepository;

public class GithubRepositoryService {
	private static final Logger LOGGER = Logger.getLogger(GithubRepositoryService.class.getName());
	private GHRepository repository;
	private GitHub github;
	private String repoUrl;
	private GithubAccessTokenRepository githubAccessTokenRepository;

	public GithubRepositoryService(GHRepository repository) {
		this(repository, new GithubAccessTokenRepository());
	}

	protected GithubRepositoryService(GHRepository repository, GithubAccessTokenRepository githubAccessTokenRepository) {
		this.repository = repository;
		this.githubAccessTokenRepository = githubAccessTokenRepository;

	}

	/**
	 * GHRepository is lazily intialized when this constructor is used
	 * 
	 * @param repoUrl
	 */
	public GithubRepositoryService(String repoUrl) {
		this.repoUrl = repoUrl;
	}

	private static GitHub getGithub(String token) {
		try {
			return GitHub.connectUsingOAuth(SetupConfig.get().getGithubApiUrl(), token);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addHook() throws IOException {
		String githubCallbackUrl = getSetupConfig().getGithubCallbackUrl();
		if (!githubCallbackUrl.endsWith("/")) {
			githubCallbackUrl = githubCallbackUrl + "/";
		}
		Map<String, String> params = ImmutableMap.of("url", githubCallbackUrl);
		List<GHEvent> events = Arrays.asList(GHEvent.PUSH, GHEvent.PULL_REQUEST);
		githubAccessTokenRepository.put(getRepository().getUrl());
		removeExistingHook();
		getRepository().createHook("web", params, events, true);
	}

	private void removeExistingHook() throws IOException {
		for (GHHook hook : getRepository().getHooks()) {
			if (hook.isActive() && getSetupConfig().getGithubCallbackUrl().equals(hook.getConfig().get("url"))) {
				hook.delete();
			}
		}
	}

	protected SetupConfig getSetupConfig() {
		return SetupConfig.get();
	}

	public GHRef getRef(String refName) throws IOException {
		String url = String.format("/repos/%s/%s/git/refs/%s", getRepository().getOwner().getLogin(), getRepository().getName(), refName);
		try {
			return GithubUtils.getObject(getGithub(), url, GHRef.class);
		} catch (InvocationTargetException e) {
			throw new IOException(e);
		}
	}

	public GHRepository getGithubRepository() {
		return getRepository();
	}

	public GHContent getGHFile(String fileName, String sha) throws IOException {
		return getGithubRepository().getFileContent(fileName, sha);
	}

	public boolean isHookConfigured() throws IOException {
		for (GHHook hook : getGithubRepository().getHooks()) {
			if (hook.isActive() && getSetupConfig().getGithubCallbackUrl().equals(hook.getConfig().get("url"))) {
				return true;
			}
		}
		return false;
	}

	public String getRepoLanguage() {
		return getRepository().getLanguage().toLowerCase();
	}

	public String getShaForBranch(String branch) {
		GitBranch gitBranch = new GitBranch(branch);
		if (gitBranch.isPullRequest()) {
			try {
				return getGithubRepository().getPullRequest(gitBranch.pullRequestNumber()).getHead().getSha();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			try {
				GHRef ref = getRef("heads/" + gitBranch);
				return ref.getObject().getSha();
			} catch (IOException e) {
				return gitBranch.toString();
			}
		}
	}

	private synchronized GHRepository getRepository() {
		if (repository == null) {
			String fullRepoName = new GitSshUrl(repoUrl).getFullRepoName();
			try {
				repository = getGithub().getRepository(fullRepoName);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return repository;
	}

	public synchronized GitHub getGithub() {
		if (github == null) {
			String accessToken = new GithubAccessTokenRepository().getAccessToken(repoUrl);
			github = getGithub(accessToken);
		}
		return github;
	}

	public boolean hasDockerFile(String sha) throws IOException {
		LOGGER.info("Checking for dockerfile in: " + repoUrl);
		try {
			getGHFile("Dockerfile", sha).getContent();
		} catch (FileNotFoundException e) {
			return false;
		}
		return true;

	}

	public void linkProjectToCi() throws IOException {
		addHook();
		addDeployKey();
	}

	protected void addDeployKey() throws IOException {
		if (getRepository().isPrivate()) {
			removeDeployKeyIfPreviouslyAdded();
			getRepository().addDeployKey("DotCi", getSetupConfig().getDeployKey());
		}
	}

	private void removeDeployKeyIfPreviouslyAdded() throws IOException {
		String configuredDeployKey = StringUtils.trim(getSetupConfig().getDeployKey());
		for (GHDeployKey deployKey : getRepository().getDeployKeys()) {
			// needs startsWith as email address is trimmed out
			if (configuredDeployKey.startsWith(deployKey.getKey())) {
				deployKey.delete();
			}
		}
	}
}