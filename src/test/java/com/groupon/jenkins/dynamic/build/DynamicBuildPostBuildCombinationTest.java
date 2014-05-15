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

import hudson.matrix.Combination;

import java.util.Arrays;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.testhelpers.DynamicBuildFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DynamicBuildPostBuildCombinationTest {

	@Test
	public void should_run_post_build_project_with_after_script() {
		DynamicBuildModel dynamicBuildProxy = new DynamicBuildModel(DynamicBuildFactory.newBuild().get(), null);
		Combination mainScript = new Combination(ImmutableMap.of("script", "main"));
		Combination postBuildScript = new Combination(ImmutableMap.of("script", "post_build"));
		Combination postBuildCombination = dynamicBuildProxy.getPostBuildCombination(Arrays.asList(mainScript, postBuildScript));
		assertNotNull(postBuildCombination);
		assertEquals("post_build", postBuildCombination.get("script"));
	}

}
