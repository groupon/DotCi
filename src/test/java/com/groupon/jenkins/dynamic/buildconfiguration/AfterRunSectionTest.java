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

import hudson.matrix.Combination;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.dynamic.build.execution.BuildType;

import static com.groupon.jenkins.testhelpers.TestHelpers.configListOrSingleValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AfterRunSectionTest {

	@Test
	public void should_run_post_build() {
		ConfigSection afterRunSection = new AfterRunSection(configListOrSingleValue("spec integration1", "spec integration2"));

		Combination combination = new Combination(ImmutableMap.of("script", "post_build"));
		assertTrue(afterRunSection.toScript(combination, BuildType.BareMetal).toShellScript().contains("spec integration1"));
	}

	@Test
	public void should_not_run_post_build_if_not_post_build() {
		ConfigSection afterRunSection = new AfterRunSection(configListOrSingleValue("spec integration1", "spec integration2"));

		Combination combination = new Combination(ImmutableMap.of("script", "main"));
		assertNull(afterRunSection.toScript(combination, BuildType.BareMetal));
	}
}
