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
package com.groupon.jenkins.util;

import com.groupon.jenkins.testhelpers.TestHelpers;
import hudson.EnvVars;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

import static com.groupon.jenkins.testhelpers.TestHelpers.loadFile;

public class GroovyTemplateProcessorTest {

	@Test
	public void should_filter_out_elements_that_are_filter_out_by_if_conditions() {
		EnvVars envVars = new EnvVars();
		envVars.put("DOTCI_BRANCH", "gh-pages");
		GroovyTemplateProcessor effectiveConfiguration = new GroovyTemplateProcessor(loadFile("/com/groupon/jenkins/dynamic/buildconfiguration/BuildConfigurationFilterTest/.ci_skip_versions.yml"), envVars);
		Map<?, ?> config = effectiveConfiguration.getConfig();
		assertNotNull(config);
		List<?> langugeVersions = (List<?>) ((Map<?, ?>) config.get("environment")).get("language_versions");
		assertEquals(1, langugeVersions.size());
	}

	@Test
	public void should_export_missing_variables_asis() {
		EnvVars envVars = new EnvVars();
		envVars.put("NOT_MISSING_PROP", "1.9.0");
		GroovyTemplateProcessor effectiveConfiguration = new GroovyTemplateProcessor(TestHelpers.loadFile("/com/groupon/jenkins/dynamic/buildconfiguration/BuildConfigurationFilterTest/.ci_missing_props.yml"), envVars);
		Map<?, ?> config = effectiveConfiguration.getConfig();
		assertNotNull(config);
		Map<?, ?> env = (Map<?, ?>) config.get("environment");
		List<?> versions = (List<?>) env.get("language_versions");
		assertEquals("$MISSING_PROP", versions.get(0));
		assertEquals("1.9.0", versions.get(1));
	}

	@Test
	public void should_not_subsitute_PATH() {
		EnvVars envVars = new EnvVars();
		envVars.put("PATH", "/PATH/BIN");
		GroovyTemplateProcessor effectiveConfiguration = new GroovyTemplateProcessor(TestHelpers.loadFile("/com/groupon/jenkins/dynamic/buildconfiguration/BuildConfigurationFilterTest/.ci_PATH.yml"), envVars);
		Map<?, ?> config = effectiveConfiguration.getConfig();
		assertNotNull(config);
		Object run = ((Map<?, ?>) config.get("build")).get("run");
		assertEquals("echo $PATH", run);
	}

    @Test
    public void should_return_null_for_missing_DOTCI_vars() {
        EnvVars envVars = new EnvVars();
        envVars.put("DOTCI_PULL_REQUEST", "1");
        GroovyTemplateProcessor effectiveConfiguration = new GroovyTemplateProcessor(TestHelpers.loadFile("/com/groupon/jenkins/dynamic/buildconfiguration/BuildConfigurationFilterTest/.ci_pr.yml"), envVars);
        Map<?, ?> config = effectiveConfiguration.getConfig();
        assertNotNull(config);
        Object run = ((Map<?, ?>) config.get("build")).get("run");
        assertEquals("echo pr 1", run);
    }
}
