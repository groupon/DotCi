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

import com.groupon.jenkins.dynamic.buildconfiguration.template.DotCiTemplate;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import hudson.EnvVars;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.kohsuke.github.GHContent;

public class EffectiveBuildConfigurationCalculator {

//	private static String baseYml;
//	static final Map<String, String> languageBaseYmls = new HashMap<String, String>();
//	private static final String YML_DIR_PATH = "/com/groupon/jenkins/buildconfiguration/base_yml/";
//
//	static {
//		loadLanguageBaseYmls();
//	}
//
//	protected static void loadLanguageBaseYmls() {
//		baseYml = readFile(YML_DIR_PATH + ".ci.yml");
//		String[] languages = { "clojure", "golang", "java", "node", "ruby", "UNKNOWN" };
//		for (String language : languages) {
//			String ymlContents = readFile(YML_DIR_PATH + "language/.ci.base." + language + ".yml");
//			languageBaseYmls.put(language, ymlContents);
//		}
//	}
//
//	private static String readFile(String ymlResource) {
//		InputStream base = null;
//		try {
//			base = EffectiveBuildConfigurationCalculator.class.getResourceAsStream(ymlResource);
//			return IOUtils.toString(base);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		} finally {
//			IOUtils.closeQuietly(base);
//		}
//	}

//	public BuildConfiguration getMergedTemplate(BuildConfiguration specifiedConfiguration, String language, EnvVars environment) {
//		BuildConfiguration baseConfiguation = getMergedTemplate(language, environment);
//		baseConfiguation.merge(specifiedConfiguration);
//		return baseConfiguation;
//	}
//
//	public BuildConfiguration getMergedTemplate(String language, EnvVars envVars) {
//		BuildConfiguration languageBuildConfiguration = new BuildConfiguration(languageBaseYmls.getMergedTemplate(language), envVars);
//		BuildConfiguration baseConfiguration = new BuildConfiguration(baseYml, envVars);
//		baseConfiguration.merge(languageBuildConfiguration);
//		return baseConfiguration;
//	}

	public BuildConfiguration calculateBuildConfiguration(String githubRepoUrl, String sha, EnvVars envVars) throws IOException, InterruptedException, InvalidDotCiYmlException {
		GithubRepositoryService githubRepositoryService = getGithubRepositoryService(githubRepoUrl);
		try {
			GHContent file = githubRepositoryService.getGHFile(".ci.yml", sha);
			BuildConfiguration configuration = new BuildConfiguration(file.getContent(), envVars);
			if (!configuration.isValid()) {
				throw new InvalidDotCiYmlException(configuration.getValidationErrors());
			}
		//	String language = configuration.getLanguage() == null ? autoDetectedLanguage(githubRepositoryService) : configuration.getLanguage();
            //DotCiTemplate languageTemplate = DotCiTemplate.getMergedTemplate(configuration.getLanguage(),githubRepositoryService.getGithubRepository());
			return DotCiTemplate.getMergedTemplate(configuration, configuration.getLanguage(), envVars);
		} catch (FileNotFoundException e) {
			return    null; //DotCiTemplate.get(githubRepositoryService.getGithubRepository(),envVars) ;
		}
	}

	protected GithubRepositoryService getGithubRepositoryService(String githubRepoUrl) {
		return new GithubRepositoryService(githubRepoUrl);
	}

//	private String autoDetectedLanguage(GithubRepositoryService githubRepositoryService) {
//		String repoLanguage = githubRepositoryService.getRepoLanguage();
//		if ("javascript".equals(repoLanguage) || "coffeescript".equals(repoLanguage)) {
//			repoLanguage = "node";
//		}
//		return languageBaseYmls.containsKey(repoLanguage) ? repoLanguage : "UNKNOWN";
//	}
}
