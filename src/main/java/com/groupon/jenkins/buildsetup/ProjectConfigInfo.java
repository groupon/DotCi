package com.groupon.jenkins.buildsetup;

import java.io.IOException;

import jenkins.model.Jenkins;

import org.kohsuke.github.GHRepository;

import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import com.groupon.jenkins.github.services.GithubAccessTokenRepository;

public class ProjectConfigInfo {

    private final String name;
    private final GHRepository repository;
    private final DynamicProjectRepository dynamicProjectRepository;
    private final GithubAccessTokenRepository githubAccessTokenRepository;
    private final GithubRepositoryService githubRepositoryService;

    public ProjectConfigInfo(String ghRepoName, GHRepository ghRepository) {
        this(ghRepoName, ghRepository, new DynamicProjectRepository(), new GithubAccessTokenRepository(), new GithubRepositoryService(ghRepository));
    }

    protected ProjectConfigInfo(String ghRepoName, GHRepository ghRepository, DynamicProjectRepository dynamicProjectRepository, GithubAccessTokenRepository githubAccessTokenRepository, GithubRepositoryService githubRepositoryService) {
        this.name = ghRepoName;
        this.repository = ghRepository;
        this.dynamicProjectRepository = dynamicProjectRepository;
        this.githubAccessTokenRepository = githubAccessTokenRepository;
        this.githubRepositoryService = githubRepositoryService;
    }

    public boolean isHookConfigured() throws IOException {
        return githubAccessTokenRepository.isConfigured(repository.getUrl());
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
        return githubAccessTokenRepository.getAssociatedLogin(repository.getUrl());
    }

    public String getJenkinsRootUrl() {
        return Jenkins.getInstance().getRootUrlFromRequest();
    }

    public String getName() {
        return name;
    }
}
