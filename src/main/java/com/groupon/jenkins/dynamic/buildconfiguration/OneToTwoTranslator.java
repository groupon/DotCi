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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class OneToTwoTranslator {

	private final Map<?, ?> config;
	private final EnvVars envVars;

	public OneToTwoTranslator(Map config, EnvVars envVars) {
		this.config = config;
		this.envVars = envVars;
	}

	public Map getConfig() {
		return needsTranslation() ? translatedConfig() : config;
	}

	private Map translatedConfig() {
		Map translatedConfig = new HashMap();
		translatedConfig.put("environment", getEnvironment());
		translatedConfig.put("build", getBuild());
		if (getNotifications() != null) {
			translatedConfig.put("notifications", getNotifications());
		}
		if (config.containsKey("output_files")) {
			translatedConfig.put("plugins", Arrays.asList(ImmutableMap.of("output_files", config.get("output_files"))));
		}

		return translatedConfig;
	}

	private Object getNotifications() {
		String currentBranch = envVars.get("BRANCH");
		if (config.containsKey("notifications")) {
			List<Object> twoPointONotifications = new ArrayList<Object>();
			Map<String, Object> notifications = (Map<String, Object>) config.get("notifications");
			for (Map.Entry<String, Object> entry : notifications.entrySet()) {
				if (matches(entry.getKey(), currentBranch)) {
					twoPointONotifications.add(entry.getValue());
				}
			}

			return twoPointONotifications.isEmpty() ? null : twoPointONotifications;
		}
		return null;
	}

	private Map getBuild() {
		Map<String, Object> build = new HashMap<String, Object>();
		Object script = config.get("script");
		if (script instanceof List) {
			Map<String, String> runConfig = new HashMap<String, String>();
			for (Map<String, String> runKey : ((List<Map<String, String>>) script)) {
				runConfig.putAll(runKey);
			}
			build.put("run", runConfig);
		} else {
			build.put("run", script);
		}

		if (isSkipped(envVars.get("BRANCH"))) {
			build.put("skip", "true");
		}
		return build;
	}

	private Map getEnvironment() {
		Map environment = new HashMap();
		Object language = config.get("language");
		if ("nodejs".equals(language)) {
			language = "node";
		}
		environment.put("language", language);
		Object languageVersions = getLanguageVersions();
		if (languageVersions != null) {
			List<String> versions;
			if (languageVersions instanceof String) {
				versions = Lists.newArrayList(getVersion((String) languageVersions));
			} else {
				versions = Lists.newArrayList(Iterables.transform(((Iterable<String>) languageVersions), new Function<String, String>() {
					@Override
					public String apply(String input) {
						return getVersion(input);
					}
				}));
			}
			environment.put("language_versions", versions);
		}
		if (config.containsKey("services")) {
			environment.put("packages", config.get("services"));
		}
		return environment;
	}

	private String getVersion(String languageVersion) {
		if (languageVersion.startsWith("ree") || languageVersion.startsWith("jruby")) {
			return languageVersion;
		}
		String language = languageVersion.split("-")[0];
		return languageVersion.replace(language + "-", "");
	}

	private Object getLanguageVersions() {
		Object result = config.get("ruby_version");
		if (result == null) {
			result = config.get("language_version");
		}
		return result;
	}

	public boolean needsTranslation() {
		return config.get("language") != null;
	}

	public boolean isSkipped(String branch) {
		List<String> branches = getConfig("branches");
		if (branches == null) {
			return false; // nothing configured, so dont skip anything
		}
		for (String configBranch : branches) {
			if (matches(configBranch, branch)) {
				return false;
			}

		}
		return true;
	}

	private boolean matches(String configBranch, String branch) {
		boolean isNegative = false;
		if (configBranch.matches("^!.*")) {
			isNegative = true;
			configBranch = configBranch.replaceFirst("^!", "");
		}
		branch = normalizeBranchName(branch);

		if (isRegex(configBranch)) {
			configBranch = configBranch.replaceFirst("^/", "").replaceFirst("/$", "");
			return isNegative ? !branch.matches(configBranch) : branch.matches(configBranch);
		}
		return isNegative ? !configBranch.equals(branch) : configBranch.equals(branch);
	}

	private String normalizeBranchName(String branch) {
		return branch.replace("origin/", "");
	}

	private boolean isRegex(String configBranch) {
		return configBranch.matches("^/.*/$");
	}

	private List<String> getConfig(String configParam) {
		Object configValue = config.get(configParam);
		if (configValue == null) {
			return null;
		}
		if (configValue instanceof List) {
			return (List<String>) configValue;
		} else {
			return Arrays.asList(configValue.toString());
		}

	}

}
