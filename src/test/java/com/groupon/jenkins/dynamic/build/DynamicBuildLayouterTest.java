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
package com.groupon.jenkins.dynamic.build;

import hudson.matrix.Axis;
import hudson.matrix.AxisList;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import com.google.common.collect.Lists;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicBuildLayouter;
import com.groupon.jenkins.dynamic.buildconfiguration.BuildConfiguration;

import static org.junit.Assert.assertEquals;

public class DynamicBuildLayouterTest {

	private DynamicBuild dynamicBuild;
	private BuildConfiguration buildConfiguration;

	@Before
	public void setUpMocks() {
		dynamicBuild = mock(DynamicBuild.class);
		buildConfiguration = mock(BuildConfiguration.class);
		when(dynamicBuild.getBuildConfiguration()).thenReturn(buildConfiguration);

	}

	@Test
	public void shouldHaveScriptAxisIfParallizingOnScripts() throws IOException {
		when(buildConfiguration.isMultiScript()).thenReturn(true);
		when(buildConfiguration.getScriptKeys()).thenReturn(Lists.newArrayList("a", "b"));

		AxisList axisList = DynamicBuildLayouter.calculateAxisList(dynamicBuild);

		assertEquals(1, axisList.size());
		Axis scriptAxis = axisList.get(0);
		assertEquals(scriptAxis.getName(), "script");
		assertEquals(2, scriptAxis.size());
	}

	@Test
	public void shouldHaveRubyAxisIfParallizingOnRubyVersions() throws IOException {
		when(buildConfiguration.isMultiLanguageVersions()).thenReturn(true);
		when(buildConfiguration.getLanguageVersions()).thenReturn(Lists.newArrayList("a", "b"));

		AxisList axisList = DynamicBuildLayouter.calculateAxisList(dynamicBuild);
		assertEquals(1, axisList.size());
		Axis scriptAxis = axisList.get(0);
		assertEquals(scriptAxis.getName(), "language_version");
		assertEquals(2, scriptAxis.size());
	}

	@Test
	public void shouldHaveBothScriptAndRubyAxisIfParallizingOnRubyVersionsAndScripts() throws IOException {
		when(buildConfiguration.isMultiScript()).thenReturn(true);
		when(buildConfiguration.getScriptKeys()).thenReturn(Lists.newArrayList("a", "b"));
		when(buildConfiguration.isMultiLanguageVersions()).thenReturn(true);
		when(buildConfiguration.getLanguageVersions()).thenReturn(Lists.newArrayList("a", "b"));

		AxisList axisList = DynamicBuildLayouter.calculateAxisList(dynamicBuild);

		assertEquals(2, axisList.size());
		assertEquals(axisList.get(0).getName(), "language_version");
		assertEquals(axisList.get(1).getName(), "script");

	}

}
