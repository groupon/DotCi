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
import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class SetupConfig extends GlobalConfiguration {
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
    public static SetupConfig get() {
        return GlobalConfiguration.all().get(SetupConfig.class);
    }

    public SetupConfig() {
        load();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return true;
    }

    public Iterable<BuildType> getBuildTypes(){
        return BuildType.all();
    }
    public String getDbName() {
        if (StringUtils.isEmpty(dbName)) {
            return "dotci";
        }
        return dbName;
    }


    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbHost() {
        if (StringUtils.isEmpty(dbHost)) {
            return "localhost";
        }
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public int getDbPort() {
        if (dbPort == 0) {
            return 27017;
        }
        return dbPort;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }



    public void setGithubApiUrl(String githubApiUrl) {
        this.githubApiUrl = githubApiUrl;
    }

    public String getLabel() {
        return StringUtils.trimToEmpty(this.label);
    }

    public String getGithubCallbackUrl() {
        if (StringUtils.isEmpty(githubCallbackUrl)) {
            return Jenkins.getInstance().getRootUrl() + "githook/";
        }
        return this.githubCallbackUrl;
    }

    public void setGithubCallbackUrl(String githubCallbackUrl) {
        this.githubCallbackUrl = githubCallbackUrl;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFromEmailAddress() {
        if (StringUtils.isEmpty(fromEmailAddress)) {
            return "ci@example.com";
        }
        return fromEmailAddress;
    }

    public void setFromEmailAddress(String fromEmailAddress) {
        this.fromEmailAddress = fromEmailAddress;
    }


    public boolean hasPrivateRepoSupport() {
        return getPrivateRepoSupport();
    }


    public String getDefaultBuildType() {
        if (StringUtils.isEmpty(defaultBuildType)) {
            DockerComposeBuild type = new DockerComposeBuild();
            return type.getId();
        }
        return defaultBuildType;
    }

    public void setDefaultBuildType(String defaultBuildType) {
        this.defaultBuildType = defaultBuildType;
    }

    public void setPrivateRepoSupport(boolean privateRepoSupport) {
        this.privateRepoSupport = privateRepoSupport;
    }
    public boolean getPrivateRepoSupport() {
        return privateRepoSupport;
    }

    public String getGithubApiUrl() {
        if (StringUtils.isEmpty(githubApiUrl)) {
            return "https://api.github.com/";
        }
        return githubApiUrl;
    }

    public String getGithubWebUrl() {
        if (StringUtils.isEmpty(githubWebUrl)) {
            return "https://github.com/";
        }
        return githubWebUrl;
    }

    public void setGithubWebUrl(String githubWebUrl) {
        this.githubWebUrl = githubWebUrl;
    }


    public String getGithubClientID() {
        return githubClientID;
    }

    public void setGithubClientID(String githubClientID) {
        this.githubClientID = githubClientID;
    }

    public String getGithubClientSecret() {
        return githubClientSecret;
    }

    public void setGithubClientSecret(String githubClientSecret) {
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
        return defaultToNewUi;
    }

    public void setDefaultToNewUi(boolean defaultToNewUi) {
        this.defaultToNewUi = defaultToNewUi;
    }

    private transient Object injectorLock = new Object();

    public Injector getInjector() {
        if(injector == null) {
            synchronized (injectorLock) {
                if(injector == null) { // make sure we got the lock in time
                    injector = Guice.createInjector(getGuiceModule());
                }
            }
        }

        return injector;
    }


    private AbstractModule getGuiceModule() {
        if(guiceModule == null) {
            return new DotCiModule();
        }
        return guiceModule;
    }

    public GithubDeployKeyRepository getGithubDeployKeyRepository() {
        return getInjector().getInstance(GithubDeployKeyRepository.class);
    }
}
