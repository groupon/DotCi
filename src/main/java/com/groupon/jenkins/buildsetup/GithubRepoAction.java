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

package com.groupon.jenkins.buildsetup;

import com.groupon.jenkins.SetupConfig;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.stapler.StaplerRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

public abstract class GithubRepoAction implements ExtensionPoint {
    public static List<GithubRepoAction> getGithubRepoActions() {
        return Jenkins.getInstance().getExtensionList(GithubRepoAction.class);
    }

    public String getHtml(ProjectConfigInfo projectConfigInfo) throws IOException, ClassNotFoundException, JellyException {
        String name = getClass().getName().replace('.', '/').replace('$', '/') + "/" + "index.jelly";
        URL actionTemplate = getClass().getClassLoader().getResource(name);
        JellyContext context = new JellyContext();
        context.setVariable("p", projectConfigInfo);
        context.setVariable("it", this);
        OutputStream outputStream = new ByteArrayOutputStream();
        XMLOutput output = XMLOutput.createXMLOutput(outputStream);
        context.runScript(actionTemplate, output);
        output.flush();
        return "<p>" + outputStream.toString() + " </p>";
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    protected GHRepository getGithubRepository(StaplerRequest request) throws IOException {
        String repoName = request.getParameter("fullName");

        GitHub github = getGitHub(request);
        return github.getRepository(repoName);
    }

    protected GitHub getGitHub(StaplerRequest request) throws IOException {
        return GitHub.connectUsingOAuth(getSetupConfig().getGithubApiUrl(), getAccessToken(request));
    }

    protected SetupConfig getSetupConfig() {
        return SetupConfig.get();
    }

    protected String getAccessToken(StaplerRequest request) {
        return (String) request.getSession().getAttribute("access_token");
    }

    protected String getCurrentUserLogin(StaplerRequest request) throws IOException {
        GHUser self = GitHub.connectUsingOAuth(getSetupConfig().getGithubApiUrl(), getAccessToken(request)).getMyself();
        return self.getLogin();
    }
}
