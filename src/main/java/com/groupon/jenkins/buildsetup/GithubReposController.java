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
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import com.groupon.jenkins.github.services.GithubCurrentUserService;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import com.groupon.jenkins.util.AuthenticatedRootAction;
import hudson.Extension;
import hudson.model.TopLevelItem;
import hudson.security.SecurityRealm;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jenkins.model.Jenkins;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.GithubAuthenticationToken;
import org.jenkinsci.plugins.GithubSecurityRealm;
import org.kohsuke.github.GHRepository;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;

@Extension
public class GithubReposController extends AuthenticatedRootAction {

    @Override
    public String getIconFileName() {
        return "new-package.png";
    }

    @Override
    public String getDisplayName() {
        return "New DotCi Job";
    }

    @Override
    public String getUrlName() {
        return "mygithubprojects";
    }

    public Iterable<String> getOrgs() {
        return getCurrentUser().getOrgs();
    }

    public Iterable<ProjectConfigInfo> getRepositories() {
        List<ProjectConfigInfo> projectInfos = new LinkedList<ProjectConfigInfo>();
        Map<String, GHRepository> ghRepos = getCurrentUser().getRepositories(getCurrentOrg());
        for (Map.Entry<String, GHRepository> entry : ghRepos.entrySet()) {
            if (entry.getValue().hasAdminAccess()) {
                projectInfos.add(new ProjectConfigInfo(entry.getKey(), entry.getValue()));
            }
        }
        return projectInfos;
    }

    protected GithubCurrentUserService getCurrentUser() {
        return GithubCurrentUserService.current();
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        String org = req.getRestOfPath().replace("/", "");
        req.getSession().setAttribute("setupOrg" + this.getCurrentGithubLogin(), org);
        rsp.forwardToPreviousPage(req);
    }

    private String getCurrentGithubLogin() {
        return getCurrentUser().getCurrentLogin();
    }

    public String getCurrentOrg() {
        String currentOrg = (String) Stapler.getCurrentRequest().getSession().getAttribute("setupOrg" + getCurrentGithubLogin());
        return StringUtils.isEmpty(currentOrg) ? Iterables.get(getOrgs(), 0) : currentOrg;
    }

    public void doCreateProject(StaplerRequest request, StaplerResponse response) throws IOException {
        DynamicProject project = SetupConfig.get().getDynamicProjectRepository().createNewProject(getGithubRepository(request));
        response.sendRedirect2(redirectAfterCreateItem(request, project));
    }

    private GHRepository getGithubRepository(StaplerRequest request) throws IOException {
        String repoName = request.getParameter("fullName");
        GithubAuthenticationToken auth = (GithubAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return auth.getGitHub().getRepository(repoName);
    }

    public void doRefreshHook(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
        new GithubRepositoryService(getGithubRepository(request)).addHook();
        response.forwardToPreviousPage(request);
    }

    protected String redirectAfterCreateItem(StaplerRequest req, TopLevelItem result) throws IOException {
        return Jenkins.getInstance().getRootUrlFromRequest() + "/" + result.getUrl();
    }

    @Override
    public String getSearchUrl() {
        return getUrlName();
    }
    public boolean isSecurityConfigured(){
        SecurityRealm securityRealm = Jenkins.getInstance().getSecurityRealm();
        return securityRealm instanceof GithubSecurityRealm;
    }

}
