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
package com.groupon.jenkins.dynamic.build.execution;

import hudson.Launcher;
import hudson.model.BuildListener;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.buildconfiguration.BuildConfiguration;
import com.groupon.jenkins.dynamic.buildconfiguration.plugins.DotCiPluginAdapter;

public class DotCiPluginRunner {
	public static final DotCiPluginRunner NOOP = new DotCiPluginRunner(null, null, null) {
		@Override
		public void runPlugins(BuildListener listener) {
		};
	};
	private final BuildConfiguration buildConfiguration;
	private final DynamicBuild build;
	private final Launcher launcher;

	public DotCiPluginRunner(DynamicBuild build, Launcher launcher, BuildConfiguration buildConfiguration) {
		this.build = build;
		this.launcher = launcher;
		this.buildConfiguration = buildConfiguration;
	}

	public void runPlugins(BuildListener listener) {
		if (buildConfiguration != null) {
			for (DotCiPluginAdapter plugin : buildConfiguration.getPlugins()) {
				plugin.perform(build, launcher, listener);
			}
		}
	}

}
