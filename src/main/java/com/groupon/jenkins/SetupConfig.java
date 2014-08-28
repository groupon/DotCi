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

import com.groupon.jenkins.dynamic.buildtype.BuildType;
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
	private String githubApiUrl;
	private String githubCallbackUrl;
	private String label;
	private String fromEmailAddress;
    private String defaultBuildType;
	private String deployKey;

	public static SetupConfig get() {
		return GlobalConfiguration.all().get(SetupConfig.class);
	}

	public SetupConfig() {
		load();
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
		JSONObject privateRepoSupportJson = (JSONObject) json.get("privateRepoSupport");
		if (privateRepoSupportJson != null) {
			deployKey = privateRepoSupportJson.getString("deployKey");
			json.remove("privateRepoSupport");
		} else {
			deployKey = null;
		}
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

	public String getGithubApiUrl() {
		if (StringUtils.isEmpty(githubApiUrl)) {
			return "https://api.github.com/";
		}
		return githubApiUrl;
	}

	public void setGithubApiUrl(String githubApiUrl) {
		this.githubApiUrl = githubApiUrl;
	}

	public String getLabel() {
		return StringUtils.trimToEmpty(this.label);
	}

	public String getGithubCallbackUrl() {
		if (StringUtils.isEmpty(githubCallbackUrl)) {
			return Jenkins.getInstance().getRootUrl() + "/githook/";
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

	// for EL
	public boolean getPrivateRepoSupport() {
		return deployKey != null;
	}

	public boolean hasPrivateRepoSupport() {
		return getPrivateRepoSupport();
	}

	public String getDeployKey() {
		return deployKey;
	}

	public void setDeployKey(String deployKey) {
		this.deployKey = deployKey;
	}

    public String getDefaultBuildType() {
        if (StringUtils.isEmpty(defaultBuildType)) {
            return BuildType.all().get(0).getId();
        }
        return defaultBuildType;
    }

    public void setDefaultBuildType(String defaultBuildType) {
        this.defaultBuildType = defaultBuildType;
    }
}