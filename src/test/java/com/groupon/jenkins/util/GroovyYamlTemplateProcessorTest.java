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

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GroovyYamlTemplateProcessorTest {

    @Test
    public void should_filter_out_elements_that_are_filter_out_by_if_conditions() {
        final Map<String, Object> envVars = new HashMap<>();
        envVars.put("DOTCI_BRANCH", "gh-pages");
        final GroovyYamlTemplateProcessor effectiveConfiguration = new GroovyYamlTemplateProcessor(ResourceUtils.readResource(getClass(), ".ci_skip_versions.yml"), envVars);
        final Map<?, ?> config = effectiveConfiguration.getConfig();
        assertNotNull(config);
        final List<?> langugeVersions = (List<?>) ((Map<?, ?>) config.get("environment")).get("language_versions");
        assertEquals(1, langugeVersions.size());
    }

    @Test
    public void should_export_missing_variables_asis() {
        final Map<String, Object> envVars = new HashMap<>();
        envVars.put("NOT_MISSING_PROP", "1.9.0");
        final GroovyYamlTemplateProcessor effectiveConfiguration = new GroovyYamlTemplateProcessor(ResourceUtils.readResource(getClass(), ".ci_missing_props.yml"), envVars);
        final Map<?, ?> config = effectiveConfiguration.getConfig();
        assertNotNull(config);
        final Map<?, ?> env = (Map<?, ?>) config.get("environment");
        final List<?> versions = (List<?>) env.get("language_versions");
        assertEquals("$MISSING_PROP", versions.get(0));
        assertEquals("1.9.0", versions.get(1));
    }

    @Test
    public void should_not_subsitute_PATH() {
        final Map<String, Object> envVars = new HashMap<>();
        envVars.put("PATH", "/PATH/BIN");
        final GroovyYamlTemplateProcessor effectiveConfiguration = new GroovyYamlTemplateProcessor(ResourceUtils.readResource(getClass(), ".ci_PATH.yml"), envVars);
        final Map<?, ?> config = effectiveConfiguration.getConfig();
        assertNotNull(config);
        final Object run = ((Map<?, ?>) config.get("build")).get("run");
        assertEquals("echo $PATH", run);
    }

    @Test
    public void should_return_null_for_missing_DOTCI_vars() {
        final Map<String, Object> envVars = new HashMap<>();
        envVars.put("DOTCI_PULL_REQUEST", "1");
        final GroovyYamlTemplateProcessor effectiveConfiguration = new GroovyYamlTemplateProcessor(ResourceUtils.readResource(getClass(), ".ci_pr.yml"), envVars);
        final Map<?, ?> config = effectiveConfiguration.getConfig();
        assertNotNull(config);
        final Object run = ((Map<?, ?>) config.get("build")).get("run");
        assertEquals("echo pr 1", run);
    }
}
