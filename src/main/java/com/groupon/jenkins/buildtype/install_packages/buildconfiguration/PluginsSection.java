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
package com.groupon.jenkins.buildtype.install_packages.buildconfiguration;

import com.google.common.collect.Iterables;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListValue;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import hudson.matrix.Combination;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginsSection extends ConfigSection<ListValue<?>> {

	public static final String NAME = "plugins";

	public PluginsSection(ListValue<?> configValue) {
		super(NAME, configValue, MergeStrategy.APPEND);
	}

	public List<DotCiPluginAdapter> getPlugins() {
		if (getConfigValue().isEmpty()) {
			return Collections.emptyList();
		}
		List<?> pluginSpecs = getConfigValue().getValue();
		List<DotCiPluginAdapter> plugins = new ArrayList<DotCiPluginAdapter>(pluginSpecs.size());
		for (Object pluginSpec : pluginSpecs) {
			String pluginName;
			Object options;
			if (pluginSpec instanceof String) {
				pluginName = (String) pluginSpec;
				options = new HashMap<Object, Object>();
			} else { // has to be a Map
				Map<String, Object> pluginSpecMap = (Map<String, Object>) pluginSpec;
				pluginName = Iterables.getOnlyElement(pluginSpecMap.keySet());
				options = pluginSpecMap.get(pluginName);
			}
			plugins.add(createPlugin(pluginName, options));
		}
		return plugins;
	}

	protected DotCiPluginAdapter createPlugin(String name, Object options) {
		return DotCiPluginAdapter.create(name, options);
	}

	@Override
	public ShellCommands toScript(Combination combination) {
		return ShellCommands.NOOP;
	}

}
