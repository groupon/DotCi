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
package com.groupon.jenkins;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.groupon.jenkins.buildtype.dockercompose.DockerComposeBuild;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import com.groupon.jenkins.dynamic.buildtype.BuildType;
import com.groupon.jenkins.github.services.GithubAccessTokenRepository;
import com.groupon.jenkins.github.services.GithubDeployKeyRepository;
import com.mongodb.ServerAddress;
import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Extension
public class SetupConfig extends GlobalConfiguration {
    private final transient Object injectorLock = new Object();
    private String dbHost;
    private int dbPort;
    private String dbName;
    private String label;
    private String fromEmailAddress;
    private String defaultBuildType;
    private boolean privateRepoSupport;
    private boolean defaultToNewUi;
    private String githubApiUrl;
    private String githubWebUrl;
    private String githubClientID;
    private String githubClientSecret;
    private String deployKey;
    private AbstractModule guiceModule;
    private transient Injector injector;
    private String githubCallbackUrl;

    public SetupConfig() {
        load();
    }

    // For tests only
    protected SetupConfig(final String nnul) {
    }

    public static SetupConfig get() {
        return GlobalConfiguration.all().get(SetupConfig.class);
    }

    @Override
    public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return true;
    }

    public Iterable<BuildType> getBuildTypes() {
        return BuildType.all();
    }

    public String getDbName() {
        if (StringUtils.isEmpty(this.dbName)) {
            return "dotci";
        }
        return this.dbName;
    }

    public void setDbName(final String dbName) {
        this.dbName = dbName;
    }

    public String getDbHost() {
        if (StringUtils.isEmpty(this.dbHost)) {
            return "localhost";
        }
        return this.dbHost;
    }

    public void setDbHost(final String dbHost) {
        this.dbHost = dbHost;
    }

    public int getDbPort() {
        if (this.dbPort == 0) {
            return 27017;
        }
        return this.dbPort;
    }

    public void setDbPort(final int dbPort) {
        this.dbPort = dbPort;
    }

    public String getLabel() {
        return StringUtils.trimToEmpty(this.label);
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getGithubCallbackUrl() {
        if (StringUtils.isEmpty(this.githubCallbackUrl)) {
            return Jenkins.getInstance().getRootUrl() + "githook/";
        }
        return this.githubCallbackUrl;
    }

    public void setGithubCallbackUrl(final String githubCallbackUrl) {
        this.githubCallbackUrl = githubCallbackUrl;
    }

    public String getFromEmailAddress() {
        if (StringUtils.isEmpty(this.fromEmailAddress)) {
            return "ci@example.com";
        }
        return this.fromEmailAddress;
    }

    public void setFromEmailAddress(final String fromEmailAddress) {
        this.fromEmailAddress = fromEmailAddress;
    }


    public boolean hasPrivateRepoSupport() {
        return getPrivateRepoSupport();
    }


    public String getDefaultBuildType() {
        if (StringUtils.isEmpty(this.defaultBuildType)) {
            final DockerComposeBuild type = new DockerComposeBuild();
            return type.getId();
        }
        return this.defaultBuildType;
    }

    public void setDefaultBuildType(final String defaultBuildType) {
        this.defaultBuildType = defaultBuildType;
    }

    public boolean getPrivateRepoSupport() {
        return this.privateRepoSupport;
    }

    public void setPrivateRepoSupport(final boolean privateRepoSupport) {
        this.privateRepoSupport = privateRepoSupport;
    }

    public String getGithubApiUrl() {
        if (StringUtils.isEmpty(this.githubApiUrl)) {
            return "https://api.github.com/";
        }
        return this.githubApiUrl;
    }

    public void setGithubApiUrl(final String githubApiUrl) {
        this.githubApiUrl = githubApiUrl;
    }

    public String getGithubWebUrl() {
        if (StringUtils.isEmpty(this.githubWebUrl)) {
            return "https://github.com/";
        }
        return this.githubWebUrl;
    }

    public void setGithubWebUrl(final String githubWebUrl) {
        this.githubWebUrl = githubWebUrl;
    }

    public String getGithubClientID() {
        return this.githubClientID;
    }

    public void setGithubClientID(final String githubClientID) {
        this.githubClientID = githubClientID;
    }

    public String getGithubClientSecret() {
        return this.githubClientSecret;
    }

    public void setGithubClientSecret(final String githubClientSecret) {
        this.githubClientSecret = githubClientSecret;
    }

    public DynamicBuildRepository getDynamicBuildRepository() {
        return getInjector().getInstance(DynamicBuildRepository.class);
    }

    public DynamicProjectRepository getDynamicProjectRepository() {
        return getInjector().getInstance(DynamicProjectRepository.class);
    }

    public GithubAccessTokenRepository getGithubAccessTokenRepository() {
        return getInjector().getInstance(GithubAccessTokenRepository.class);
    }

    public boolean isDefaultToNewUi() {
        return this.defaultToNewUi;
    }

    public void setDefaultToNewUi(final boolean defaultToNewUi) {
        this.defaultToNewUi = defaultToNewUi;
    }

    public Injector getInjector() {
        if (this.injector == null) {
            synchronized (this.injectorLock) {
                if (this.injector == null) { // make sure we got the lock in time
                    this.injector = Guice.createInjector(getGuiceModule());
                }
            }
        }

        return this.injector;
    }


    private AbstractModule getGuiceModule() {
        if (this.guiceModule == null) {
            return new DotCiModule();
        }
        return this.guiceModule;
    }

    public GithubDeployKeyRepository getGithubDeployKeyRepository() {
        return getInjector().getInstance(GithubDeployKeyRepository.class);
    }

    public List<ServerAddress> getMongoServerAddresses() throws UnknownHostException {
        final List<ServerAddress> serverAddresses = new ArrayList<>();
        for (final String dbHost : getDbHost().split(",")) {
            final String[] hostPort = dbHost.split(":");
            final int port = hostPort.length == 1 ? getDbPort() : Integer.parseInt(hostPort[1]);
            serverAddresses.add(new ServerAddress(hostPort[0], port));
        }
        return serverAddresses;
    }
}
