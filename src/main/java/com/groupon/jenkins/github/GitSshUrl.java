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
package com.groupon.jenkins.github;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitSshUrl {
	private static final Pattern GITHUB_HTTP_URL = Pattern.compile("^https?://(.*)/(.*)/(.*)");
	private final String url;
	private final String orgName;
	private final String name;

	public GitSshUrl(String url) {
		Matcher matcher = GITHUB_HTTP_URL.matcher(url);
		if (matcher.matches()) {
			this.orgName = matcher.group(2);
			this.name = matcher.group(3);
			this.url = "git@" + matcher.group(1) + ":" + this.orgName + "/" + this.name + ".git";
		} else {
			this.url = url;
			if (url.split(":").length < 2) {
				throw new IllegalArgumentException("URL: " + url + " is not valid");
			}
			String[] view_name = url.split(":")[1].split("/");
			this.name = view_name[1].replace(".git", "");
			this.orgName = view_name[0];
		}
	}

	public String getOrgName() {
		return orgName;
	}

	public String getFullRepoName() {
		return orgName + "/" + name;
	}

	public String getUrl() {
		return url;
	}

}
