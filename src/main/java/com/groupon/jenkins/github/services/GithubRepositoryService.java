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

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.git.GitBranch;
import com.groupon.jenkins.git.GitUrl;
import com.groupon.jenkins.github.DeployKeyPair;
import com.groupon.jenkins.util.KeyPairGenerator;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHDeployKey;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GithubRepositoryService {
    private static final Logger LOGGER = Logger.getLogger(GithubRepositoryService.class.getName());
    private GHRepository repository;
    private GitHub github;
    private String repoUrl;
    private GithubAccessTokenRepository githubAccessTokenRepository;
    private GithubDeployKeyRepository githubDeployKeyRepository;

    public GithubRepositoryService(final GHRepository repository) {
        this(repository, SetupConfig.get().getGithubAccessTokenRepository(), SetupConfig.get().getGithubDeployKeyRepository());
    }

    protected GithubRepositoryService(final GHRepository repository, final GithubAccessTokenRepository githubAccessTokenRepository, final GithubDeployKeyRepository githubDeployKeyRepository) {
        this.repository = repository;
        this.githubAccessTokenRepository = githubAccessTokenRepository;
        this.githubDeployKeyRepository = githubDeployKeyRepository;

        this.githubDeployKeyRepository = githubDeployKeyRepository;
    }

    /**
     * GHRepository is lazily intialized when this constructor is used
     *
     * @param repoUrl
     */
    public GithubRepositoryService(final String repoUrl) {
        this.repoUrl = repoUrl;
    }

    private static GitHub getGithub(final String token) {
        try {
            return GitHub.connectUsingOAuth(SetupConfig.get().getGithubApiUrl(), token);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHook(final String accessToken, final String user) throws IOException {
        String githubCallbackUrl = getSetupConfig().getGithubCallbackUrl();
        if (!githubCallbackUrl.endsWith("/")) {
            githubCallbackUrl = githubCallbackUrl + "/";
        }
        final Map<String, String> params = ImmutableMap.of("url", githubCallbackUrl);
        final List<GHEvent> events = Arrays.asList(GHEvent.PUSH, GHEvent.PULL_REQUEST);
        this.githubAccessTokenRepository.put(getRepository().getHtmlUrl().toExternalForm(), accessToken, user);
        removeExistingHook(githubCallbackUrl);
        getRepository().createHook("web", params, events, true);
    }

    private void removeExistingHook(final String callbackUrl) throws IOException {
        for (final GHHook hook : getRepository().getHooks()) {
            if (hook.isActive() && callbackUrl.equals(hook.getConfig().get("url"))) {
                hook.delete();
            }
        }
    }

    protected SetupConfig getSetupConfig() {
        return SetupConfig.get();
    }

    public GHRef getRef(final String refName) throws IOException {
        return getRepository().getRef(refName);
    }

    public GHRepository getGithubRepository() {
        return getRepository();
    }

    public GHContent getGHFile(final String fileName, final String sha) throws IOException {
        return getGithubRepository().getFileContent(fileName, sha);
    }

    public org.kohsuke.github.GHCommit getHeadCommitForBranch(final String branch) throws IOException {
        String sha;
        final GitBranch gitBranch = new GitBranch(branch);
        if (gitBranch.isPullRequest()) {
            try {
                sha = getGithubRepository().getPullRequest(gitBranch.pullRequestNumber()).getHead().getSha();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                final GHRef ref = getRef("heads/" + gitBranch);
                sha = ref.getObject().getSha();
            } catch (final IOException e) {
                sha = gitBranch.toString();
            }
        }
        return getGithubRepository().getCommit(sha);
    }

    private synchronized GHRepository getRepository() {
        if (this.repository == null) {
            final String fullRepoName = new GitUrl(this.repoUrl).getFullRepoName();
            try {
                this.repository = getGithub().getRepository(fullRepoName);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.repository;
    }

    public synchronized GitHub getGithub() {
        if (this.github == null) {
            final String accessToken = SetupConfig.get().getGithubAccessTokenRepository().getAccessToken(this.repoUrl);
            this.github = getGithub(accessToken);
        }
        return this.github;
    }

    public boolean hasDockerFile(final String sha) throws IOException {
        LOGGER.info("Checking for dockerfile in: " + this.repoUrl);
        try {
            getGHFile("Dockerfile", sha).getContent();
        } catch (final FileNotFoundException e) {
            return false;
        }
        return true;

    }

    public void linkProjectToCi(final String accessToken, final String user) throws IOException {
        addHook(accessToken, user);
        addDeployKey();
    }

    protected void addDeployKey() throws IOException {
        if (getRepository().isPrivate()) {
            removeDeployKeyIfPreviouslyAdded();
            final DeployKeyPair keyPair = new KeyPairGenerator().generateKeyPair();
            getRepository().addDeployKey("DotCi", keyPair.publicKey);
            this.githubDeployKeyRepository.put(getRepository().getHtmlUrl().toExternalForm(), keyPair);
        }
    }

    private void removeDeployKeyIfPreviouslyAdded() throws IOException {
        for (final GHDeployKey deployKey : getRepository().getDeployKeys()) {
            if ("DotCi".equals(deployKey.getTitle())) {
                deployKey.delete();
            }
        }
    }
}
