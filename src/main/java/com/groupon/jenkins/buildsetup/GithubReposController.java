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

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Extension
public class GithubReposController implements RootAction, StaplerProxy {

    public static final String URL = "mygithubprojects";

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

    public Iterable<ProjectConfigInfo> getRepositories() throws IOException {
        List<ProjectConfigInfo> projectInfos = new LinkedList<ProjectConfigInfo>();
        Map<String, GHRepository> ghRepos = getCurrentUser().getRepositories(getCurrentOrg());
        for (Map.Entry<String, GHRepository> entry : ghRepos.entrySet()) {
            if (entry.getValue().hasAdminAccess()) {
                projectInfos.add(new ProjectConfigInfo(entry.getKey(), entry.getValue()));
            }
        }
        return projectInfos;
    }

    protected GithubCurrentUserService getCurrentUser() throws IOException {
        return new GithubCurrentUserService(getGitHub(Stapler.getCurrentRequest()));
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException, InvocationTargetException, IllegalAccessException {
        String[] tokens = req.getRestOfPath().split("/");
        String leadToken = tokens.length > 0 ? tokens[1] : null;
        GithubRepoAction repoAction = getRepoAction(leadToken);
        if (repoAction != null) {
            String methodToken = tokens.length > 1 ? tokens[2] : "index";
            String methodName = "do" + StringUtils.capitalize(methodToken);
            Method method = ReflectionUtils.getPublicMethodNamed(repoAction.getClass(), methodName);
            method.invoke(repoAction, req, rsp);
        } else {
            String orgToken = req.getRestOfPath().replace("/", "");
            req.getSession().setAttribute("setupOrg" + this.getCurrentGithubLogin(), orgToken);
            rsp.forwardToPreviousPage(req);
        }
    }

    private GithubRepoAction getRepoAction(String leadToken) {
        for (GithubRepoAction repoAction : getRepoActions()) {
            if (repoAction.getName().equals(leadToken)) return repoAction;
        }
        return null;
    }

    private String getCurrentGithubLogin() throws IOException {
        return getCurrentUser().getCurrentLogin();
    }

    public String getCurrentOrg() throws IOException {
        String currentOrg = (String) Stapler.getCurrentRequest().getSession().getAttribute("setupOrg" + getCurrentGithubLogin());
        return StringUtils.isEmpty(currentOrg) ? Iterables.get(getOrgs(), 0) : currentOrg;
    }

    public int getSelectedOrgIndex() throws IOException {
        Iterable<String> orgs = getOrgs();
        for (int i = 0; i < Iterables.size(orgs); i++) {
            if (Iterables.get(orgs, i).equals(getCurrentOrg())) return i;
        }
        return 0;
    }

    public Iterable<GithubRepoAction> getRepoActions() {
        return GithubRepoAction.getGithubRepoActions();
    }

    private GitHub getGitHub(StaplerRequest request) throws IOException {
        return GitHub.connectUsingOAuth(getSetupConfig().getGithubApiUrl(), getAccessToken(request));
    }

    private SetupConfig getSetupConfig() {
        return SetupConfig.get();
    }


    private String getAccessToken(StaplerRequest request) {
        return (String) request.getSession().getAttribute("access_token");
    }


    @Override
    public Object getTarget() {
        StaplerRequest currentRequest = Stapler.getCurrentRequest();
        if (getAccessToken(currentRequest) == null)
            return new GithubOauthLoginAction();
        return this;
    }
}
