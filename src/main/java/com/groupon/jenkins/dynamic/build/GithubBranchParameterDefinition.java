/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.groupon.jenkins.dynamic.build;

import com.groupon.jenkins.github.services.GithubRepositoryService;
import hudson.Extension;
import hudson.model.StringParameterDefinition;
import org.kohsuke.github.GHRepository;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;

public class GithubBranchParameterDefinition extends StringParameterDefinition {

    private final String githubRepoUrl;

    @DataBoundConstructor
    public GithubBranchParameterDefinition(final String name, final String defaultValue, final String description, final String githubRepoUrl) {
        super(name, defaultValue, description);
        this.githubRepoUrl = githubRepoUrl;
    }

    public GithubBranchParameterDefinition(final String name, final String defaultValue, final String githubRepoUrl) {
        super(name, defaultValue);
        this.githubRepoUrl = githubRepoUrl;
    }


    public String getGithubRepoUrl() {
        return this.githubRepoUrl;
    }

    public Iterable<String> getBranches() throws IOException {
        final ArrayList<String> branches = new ArrayList<>();
        final GHRepository githubRepo = getGhRepository();
        branches.addAll(githubRepo.getBranches().keySet());
        branches.add("Pull Request: ");
        return branches;
    }

    GHRepository getGhRepository() {
        return new GithubRepositoryService(this.githubRepoUrl).getGithubRepository();
    }

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        @Override
        public String getDisplayName() {
            return "Github Branch Selector";
        }

    }

}
