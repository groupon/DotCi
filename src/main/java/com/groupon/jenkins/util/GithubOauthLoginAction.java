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

package com.groupon.jenkins.util;

import com.google.common.base.Joiner;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.buildsetup.GithubReposController;
import hudson.Extension;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.jfree.util.Log;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

@Extension
public class GithubOauthLoginAction implements RootAction {
    private HttpPoster httpPoster;

    public GithubOauthLoginAction() {
        this(new HttpPoster());
    }

    public GithubOauthLoginAction(HttpPoster httpPoster) {
        this.httpPoster = httpPoster;
    }

    public void doIndex(StaplerRequest request, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        rsp.sendRedirect2(getSetupConfig().getGithubWebUrl() + "/login/oauth/authorize?client_id="
            + getSetupConfig().getGithubClientID() + "&scope=" + getScopes());
    }

    protected String getScopes() {
        if (getSetupConfig().hasPrivateRepoSupport()) {
            return Joiner.on(",").join(Arrays.asList("repo", "user:email"));
        }
        return Joiner.on(",").join(Arrays.asList("public_repo", "repo:status", "user:email", "read:org", "write:repo_hook"));
    }


    protected SetupConfig getSetupConfig() {
        return SetupConfig.get();
    }

    public HttpResponse doFinishLogin(StaplerRequest request, StaplerResponse rsp) throws IOException {

        String code = request.getParameter("code");

        if (code == null || code.trim().length() == 0) {
            Log.info("doFinishLogin: missing code.");
            return HttpResponses.redirectToContextRoot();
        }

        String content = postForAccessToken(code);

        String accessToken = extractToken(content);
        updateOfflineAccessTokenForUser(accessToken);
        request.getSession().setAttribute("access_token", accessToken);

        String newProjectSetupUrl = getJenkinsRootUrl() + "/" + GithubReposController.URL;
        return HttpResponses.redirectTo(newProjectSetupUrl);
    }

    protected void updateOfflineAccessTokenForUser(String accessToken) throws IOException {
        GHUser self = GitHub.connectUsingOAuth(getSetupConfig().getGithubApiUrl(), accessToken).getMyself();
        String login = self.getLogin();
        getSetupConfig().getGithubAccessTokenRepository().updateAccessToken(login, accessToken);
    }

    String getJenkinsRootUrl() {
        return Jenkins.getInstance().getRootUrl();
    }

    String postForAccessToken(String code) throws IOException {
        SetupConfig setupConfig = getSetupConfig();
        return httpPoster.post(setupConfig.getGithubWebUrl() + "/login/oauth/access_token?" + "client_id=" + setupConfig.getGithubClientID() + "&"
            + "client_secret=" + setupConfig.getGithubClientSecret() + "&" + "code=" + code, new HashMap());
    }


    private String extractToken(String content) {
        String parts[] = content.split("&");
        for (String part : parts) {
            if (content.contains("access_token")) {
                String tokenParts[] = part.split("=");
                return tokenParts[1];
            }
        }
        return null;
    }

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
        return "dotci";
    }

}
