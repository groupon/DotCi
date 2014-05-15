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
import hudson.matrix.Combination;
import hudson.matrix.Layouter;


import com.groupon.jenkins.dynamic.buildconfiguration.BuildConfiguration;

public class DynamicBuildLayouter extends Layouter<DynamicRunPtr> {

	private final DynamicBuild dynamicBuild;

	public DynamicBuildLayouter(DynamicBuild dynamicBuild) {
		super(calculateAxisList(dynamicBuild));
		this.dynamicBuild = dynamicBuild;
	}

	public static AxisList calculateAxisList(DynamicBuild dynamicBuild) {
		BuildConfiguration buildConfiguration = dynamicBuild.getBuildConfiguration();
		if (buildConfiguration.isMultiLanguageVersions() && buildConfiguration.isMultiScript()) {
			return new AxisList(new Axis("language_version", buildConfiguration.getLanguageVersions()), new Axis("script", buildConfiguration.getScriptKeys()));
		}
		if (buildConfiguration.isMultiLanguageVersions()) {
			return new AxisList(new Axis("language_version", buildConfiguration.getLanguageVersions()));
		}
		if (buildConfiguration.isMultiScript()) {
			return new AxisList(new Axis("script", buildConfiguration.getScriptKeys()));
		}
		return new AxisList(new Axis("script", "main"));
	}

	@Override
	protected DynamicRunPtr getT(Combination c) {
		return new DynamicRunPtr(c, dynamicBuild);
	}

	public static DynamicBuildLayouter get(DynamicBuild dynamicBuild) {
		return dynamicBuild.isConfigurationCalculated() ? new DynamicBuildLayouter(dynamicBuild) : null;
	}

}
