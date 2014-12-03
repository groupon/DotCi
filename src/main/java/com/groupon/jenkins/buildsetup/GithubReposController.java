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
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.github.services.GithubCurrentUserService;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import com.groupon.jenkins.util.GithubOauthLoginAction;
import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;

@Extension
public class GithubReposController implements RootAction,StaplerProxy {

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

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        String org = req.getRestOfPath().replace("/", "");
        req.getSession().setAttribute("setupOrg" + this.getCurrentGithubLogin(), org);
        rsp.forwardToPreviousPage(req);
    }

    private String getCurrentGithubLogin() throws IOException {
        return getCurrentUser().getCurrentLogin();
    }

    public String getCurrentOrg() throws IOException {
        String currentOrg = (String) Stapler.getCurrentRequest().getSession().getAttribute("setupOrg" + getCurrentGithubLogin());
        return StringUtils.isEmpty(currentOrg) ? Iterables.get(getOrgs(), 0) : currentOrg;
    }

    public void doCreateProject(StaplerRequest request, StaplerResponse response) throws IOException {
        DynamicProject project = SetupConfig.get().getDynamicProjectRepository().createNewProject(getGithubRepository(request),getAccessToken(request), getCurrentUserLogin(request));
        response.sendRedirect2(redirectAfterCreateItem(request, project));
    }

    public void doRefreshHook(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
        new GithubRepositoryService(getGithubRepository(request)).addHook(getAccessToken(request), getCurrentUserLogin(request));
        response.forwardToPreviousPage(request);
    }

    private GHRepository getGithubRepository(StaplerRequest request) throws IOException {
        String repoName = request.getParameter("fullName");

        GitHub github = getGitHub(request);
        return github.getRepository(repoName);
    }

    private GitHub getGitHub(StaplerRequest request) throws IOException {
        return GitHub.connectUsingOAuth(getSetupConfig().getGithubApiUrl(), getAccessToken(request));
    }

    private SetupConfig getSetupConfig() {
        return SetupConfig.get();
    }

    private String getCurrentUserLogin(StaplerRequest request) throws IOException {
        GHUser self = GitHub.connectUsingOAuth(getSetupConfig().getGithubApiUrl(), getAccessToken(request)).getMyself();
        return self.getLogin();
    }

    private String getAccessToken(StaplerRequest request) {
        return (String) request.getSession().getAttribute("access_token");
    }


    protected String redirectAfterCreateItem(StaplerRequest req, TopLevelItem result) throws IOException {
        return Jenkins.getInstance().getRootUrl()  + result.getUrl();
    }


    @Override
    public Object getTarget() {
        StaplerRequest currentRequest = Stapler.getCurrentRequest();
        if(getAccessToken(currentRequest) == null) return new GithubOauthLoginAction();
        return  this;
    }
}
