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
package com.groupon.jenkins.dynamic.buildconfiguration;

import hudson.EnvVars;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.groupon.jenkins.github.GHFile;
import com.groupon.jenkins.github.services.GithubRepositoryService;

public class EffectiveBuildConfigurationCalculator {

	private static String baseYml;
	static final Map<String, String> languageBaseYmls = new HashMap<String, String>();
	private static final String YML_DIR_PATH = "/com/groupon/jenkins/buildconfiguration/base_yml/";

	static {
		loadLanguageBaseYmls();
	}

	protected static void loadLanguageBaseYmls() {
		baseYml = readFile(YML_DIR_PATH + ".ci.base.yml");
		String[] languages = { "clojure", "golang", "java", "node", "ruby", "UNKNOWN" };
		for (String language : languages) {
			String ymlContents = readFile(YML_DIR_PATH + "language/.ci.base." + language + ".yml");
			languageBaseYmls.put(language, ymlContents);
		}
	}

	private static String readFile(String ymlResource) {
		InputStream base = null;
		try {
			base = EffectiveBuildConfigurationCalculator.class.getResourceAsStream(ymlResource);
			return IOUtils.toString(base);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(base);
		}
	}

	public BuildConfiguration get(BuildConfiguration specifiedConfiguration, String language, EnvVars environment) {
		BuildConfiguration baseConfiguation = get(language, environment);
		baseConfiguation.merge(specifiedConfiguration);
		return baseConfiguation;
	}

	public BuildConfiguration get(String language, EnvVars envVars) {
		BuildConfiguration languageBuildConfiguration = new BuildConfiguration(languageBaseYmls.get(language), envVars);
		BuildConfiguration baseConfiguration = new BuildConfiguration(baseYml, envVars);
		baseConfiguration.merge(languageBuildConfiguration);
		return baseConfiguration;
	}

	public BuildConfiguration calculateBuildConfiguration(String githubRepoUrl, String sha, EnvVars envVars) throws IOException, InterruptedException, InvalidDotCiYmlException {
		GithubRepositoryService githubRepositoryService = getGithubRepositoryService(githubRepoUrl);
		GHFile file = githubRepositoryService.getGHFile(".ci.yml", sha);
		if (file.exists()) {
			BuildConfiguration configuration = new BuildConfiguration(file.getContents(), envVars);
			if (!configuration.isValid()) {
				throw new InvalidDotCiYmlException(configuration.getValidationErrors());
			}
			String language = configuration.getLanguage() == null ? autoDetectedLanguage(githubRepositoryService) : configuration.getLanguage();
			return get(configuration, language, envVars);
		} else {
			return get(autoDetectedLanguage(githubRepositoryService), envVars);
		}
	}

	protected GithubRepositoryService getGithubRepositoryService(String githubRepoUrl) {
		return new GithubRepositoryService(githubRepoUrl);
	}

	private String autoDetectedLanguage(GithubRepositoryService githubRepositoryService) {
		String repoLanguage = githubRepositoryService.getRepoLanguage();
		if ("javascript".equals(repoLanguage) || "coffeescript".equals(repoLanguage)) {
			repoLanguage = "node";
		}
		return languageBaseYmls.containsKey(repoLanguage) ? repoLanguage : "UNKNOWN";
	}
}
