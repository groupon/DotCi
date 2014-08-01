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
package com.groupon.jenkins.dynamic.buildtype.installpackages.buildconfiguration;

import hudson.matrix.Combination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.groupon.jenkins.dynamic.buildtype.installpackages.buildconfiguration.configvalue.ListValue;
import com.groupon.jenkins.notifications.PostBuildNotifier;

public class NotificationsSection extends ConfigSection<ListValue<?>> {

	public static final String NAME = "notifications";

	public NotificationsSection(ListValue<?> configValue) {
		super(NAME, configValue, MergeStrategy.APPEND);
	}

	@Override
	public ShellCommands toScript(Combination combination) {
		return ShellCommands.NOOP;
	}

	public List<PostBuildNotifier> getNotifiers() {
		List<?> notifierSpecs = getConfigValue().getValue();
		List<PostBuildNotifier> notifiers = new ArrayList<PostBuildNotifier>(notifierSpecs.size());
		for (Object pluginSpec : notifierSpecs) {
			String pluginName;
			Object options;
			if (pluginSpec instanceof String) {
				pluginName = (String) pluginSpec;
				options = new HashMap<String, String>();
			} else { // has to be a Map
				Map<String, Object> pluginSpecMap = (Map<String, Object>) pluginSpec;
				pluginName = Iterables.getOnlyElement(pluginSpecMap.keySet());
				options = pluginSpecMap.get(pluginName);
			}
			notifiers.add(createNotifier(pluginName, options));
		}
		return notifiers;
	}

	protected PostBuildNotifier createNotifier(String name, Object options) {
		return PostBuildNotifier.create(name, options);
	}

}