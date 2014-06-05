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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.kohsuke.github.GHContent;

import com.groupon.jenkins.github.services.GithubRepositoryService;

import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EffectiveBuildConfigurationCalculatorTest {
	private EnvVars getVars() {
		return new EnvVars("WORKSPACE", "", "GIT_URL", "", "SHA", "");
	}

	@Test
	public void should_load_base_ymls() {
		EffectiveBuildConfigurationCalculator calculator = new EffectiveBuildConfigurationCalculator();
		assertTrue(calculator.languageBaseYmls.containsKey("ruby"));
	}

	@Test
	public void should_use_default_language_template_if_no_dotci_yml() throws IOException, InterruptedException, InvalidDotCiYmlException {
		final GithubRepositoryService githubRepositoryService = mock(GithubRepositoryService.class);
		when(githubRepositoryService.getGHFile(".ci.yml", "sha")).thenThrow(FileNotFoundException.class);
		when(githubRepositoryService.getRepoLanguage()).thenReturn("ruby");
		EffectiveBuildConfigurationCalculator calculator = new EffectiveBuildConfigurationCalculator() {
			@Override
			protected GithubRepositoryService getGithubRepositoryService(String githubrepoUrl) {
				return githubRepositoryService;
			}
		};
		BuildConfiguration buildConfiguration = calculator.calculateBuildConfiguration(null, "sha", getVars());
		assertTrue(buildConfiguration.toYaml().contains("bundle exec rake"));
	}

	@Test
	public void should_merge_specified_configuration_into_base() throws IOException, InterruptedException, InvalidDotCiYmlException {
		final GithubRepositoryService githubRepositoryService = mock(GithubRepositoryService.class);
		GHContent dotCiYml = mock(GHContent.class);
		when(githubRepositoryService.getGHFile(".ci.yml", "sha")).thenReturn(dotCiYml);
		when(githubRepositoryService.getRepoLanguage()).thenReturn("ruby");

		when(dotCiYml.getContent()).thenReturn("build:\n   run: echo blah ");

		EffectiveBuildConfigurationCalculator calculator = new EffectiveBuildConfigurationCalculator() {
			@Override
			protected GithubRepositoryService getGithubRepositoryService(String githubRepoUrl) {
				return githubRepositoryService;
			}
		};
		BuildConfiguration buildConfiguration = calculator.calculateBuildConfiguration(null, "sha", getVars());
		assertTrue(buildConfiguration.toYaml().contains("echo blah"));
	}

}
