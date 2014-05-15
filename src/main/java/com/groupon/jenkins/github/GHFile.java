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

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.codec.binary.Base64;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class GHFile {
	private String filePath;
	private String sha;
	private String content;
	private GitHub github;
	private GHRepository githubRepository;

	public GHFile(GitHub github, GHRepository githubRepository, String filePath, String sha) {
		this.github = github;
		this.githubRepository = githubRepository;
		this.filePath = filePath;
		this.sha = sha;
	}

	@SuppressWarnings("unused")
	private GHFile() {
		// for serialization
	}

	public String getContents() {
		try {
			String url = "/repos/" + githubRepository.getOwner().getLogin() + "/" + githubRepository.getName() + "/contents/" + filePath + "?ref=" + sha;
			GHFile ghFile = GithubUtils.getObject(github, url, GHFile.class);
			return ghFile.getContent();
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof FileNotFoundException) {
				return null;
			}
			throw new RuntimeException(e.getTargetException());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public String getContent() {
		byte[] decoded = Base64.decodeBase64(content);
		try {
			return new String(decoded, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean exists() {
		return getContents() != null;
	}

}
