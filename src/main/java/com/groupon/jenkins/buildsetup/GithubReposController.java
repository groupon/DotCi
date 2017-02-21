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

import com.google.common.collect.Iterables;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.github.services.GithubCurrentUserService;
import com.groupon.jenkins.util.GithubOauthLoginAction;
import hudson.Extension;
import hudson.model.RootAction;
import hudson.util.ReflectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.stream.StreamSupport;

@Extension
public class GithubReposController implements RootAction, StaplerProxy {

    public static final String URL = "mygithubprojects";
    private final String currentOrg;

    public GithubReposController() {
        this(null);
    }

    public GithubReposController(final String currentOrg) {
        this.currentOrg = currentOrg;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return URL;
    }

    public Iterable<String> getOrgs() throws IOException {
        return getCurrentUser().getOrgs();
    }

    public Iterator<ProjectConfigInfo> getRepositories() throws IOException {
        final String currentOrg = getCurrentOrg();
        final Iterable<GHRepository> ghRepos = getCurrentUser().getRepositories(currentOrg);
        return StreamSupport.stream(ghRepos.spliterator(), false).
            filter(r -> r.hasAdminAccess() && currentOrg.equals(r.getOwnerName())).map(r -> new ProjectConfigInfo(r.getName(), r)).iterator();
    }

    protected GithubCurrentUserService getCurrentUser() throws IOException {
        return new GithubCurrentUserService(getGitHub(Stapler.getCurrentRequest()));
    }

    public Object getDynamic(final String token, final StaplerRequest req, final StaplerResponse rsp) throws InvocationTargetException, IllegalAccessException {
        return new GithubReposController(token);
    }

    public void doRepoAction(final StaplerRequest req, final StaplerResponse rsp) throws InvocationTargetException, IllegalAccessException {
        final String[] tokens = StringUtils.split(req.getRestOfPath(), "/");
        final GithubRepoAction repoAction = getRepoAction(tokens[0]);
        if (repoAction != null) {
            final String methodToken = tokens.length > 1 ? tokens[1] : "index";
            final String methodName = "do" + StringUtils.capitalize(methodToken);
            final Method method = ReflectionUtils.getPublicMethodNamed(repoAction.getClass(), methodName);
            method.invoke(repoAction, req, rsp);
        }
    }

    private GithubRepoAction getRepoAction(final String leadToken) {
        for (final GithubRepoAction repoAction : getRepoActions()) {
            if (repoAction.getName().equals(leadToken)) return repoAction;
        }
        return null;
    }

    private String getCurrentGithubLogin() throws IOException {
        return getCurrentUser().getCurrentLogin();
    }

    public String getCurrentOrg() throws IOException {
        return StringUtils.isEmpty(this.currentOrg) ? getCurrentGithubLogin() : this.currentOrg;
    }

    public int getSelectedOrgIndex() throws IOException {
        final Iterable<String> orgs = getOrgs();
        for (int i = 0; i < Iterables.size(orgs); i++) {
            if (Iterables.get(orgs, i).equals(getCurrentOrg())) return i;
        }
        return 0;
    }

    public Iterable<GithubRepoAction> getRepoActions() {
        return GithubRepoAction.getGithubRepoActions();
    }

    private GitHub getGitHub(final StaplerRequest request) throws IOException {
        return GitHub.connectUsingOAuth(getSetupConfig().getGithubApiUrl(), getAccessToken(request));
    }

    private SetupConfig getSetupConfig() {
        return SetupConfig.get();
    }


    private String getAccessToken(final StaplerRequest request) {
        return (String) request.getSession().getAttribute("access_token");
    }


    @Override
    public Object getTarget() {
        final StaplerRequest currentRequest = Stapler.getCurrentRequest();
        if (getAccessToken(currentRequest) == null)
            return new GithubOauthLoginAction();
        return this;
    }
}
