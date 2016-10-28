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
package com.groupon.jenkins.buildsetup;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import com.groupon.jenkins.github.services.GithubAccessTokenRepository;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import jenkins.model.Jenkins;
import org.kohsuke.github.GHRepository;

import java.io.IOException;

public class ProjectConfigInfo {

    private final String name;
    private final GHRepository repository;
    private final DynamicProjectRepository dynamicProjectRepository;
    private final GithubAccessTokenRepository githubAccessTokenRepository;
    private final GithubRepositoryService githubRepositoryService;

    public ProjectConfigInfo(String ghRepoName, GHRepository ghRepository) {
        this(
            ghRepoName,
            ghRepository,
            SetupConfig.get().getDynamicProjectRepository(),
            SetupConfig.get().getGithubAccessTokenRepository(),
            new GithubRepositoryService(ghRepository)
        );
    }

    protected ProjectConfigInfo(String ghRepoName, GHRepository ghRepository, DynamicProjectRepository dynamicProjectRepository, GithubAccessTokenRepository githubAccessTokenRepository, GithubRepositoryService githubRepositoryService) {
        this.name = ghRepoName;
        this.repository = ghRepository;
        this.dynamicProjectRepository = dynamicProjectRepository;
        this.githubAccessTokenRepository = githubAccessTokenRepository;
        this.githubRepositoryService = githubRepositoryService;
    }

    public boolean isHookConfigured() throws IOException {
        return githubAccessTokenRepository.isConfigured(repository.getHtmlUrl().toExternalForm());
    }

    public boolean isProjectPresent() throws IOException {
        return dynamicProjectRepository.projectExists(repository);
    }

    public String getJenkinsProjectUrl() throws IOException {
        return String.format("%s/job/%s/job/%s", getJenkinsRootUrl(), repository.getOwner().getLogin(), repository.getName());
    }

    public String getFullName() throws IOException {
        return repository.getOwner().getLogin() + "/" + repository.getName();
    }

    public String getHookAssignedTo() {
        return githubAccessTokenRepository.getAssociatedLogin(repository.getHtmlUrl().toExternalForm());
    }

    private String getJenkinsRootUrl() {
        return Jenkins.getInstance().getRootUrl();
    }

    public String getName() {
        return name;
    }
}
