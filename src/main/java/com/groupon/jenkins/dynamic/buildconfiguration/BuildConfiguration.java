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

import com.groupon.jenkins.dynamic.buildconfiguration.configvalue.ListValue;
import com.groupon.jenkins.dynamic.buildconfiguration.configvalue.MapValue;
import com.groupon.jenkins.dynamic.buildconfiguration.configvalue.StringValue;
import com.groupon.jenkins.dynamic.buildconfiguration.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import hudson.EnvVars;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class BuildConfiguration extends CompositeConfigSection {

	private EnvironmentSection environmentSection;
	protected BuildSection buildSection;
	private NotificationsSection notificationsSection;
	private PluginsSection pluginsSection;
    private ParentTemplateSection parentTemplateSection;

	public BuildConfiguration(String ymlDefintion, EnvVars envVars) {
		this(translateIfOne(new BuildConfigurationFilter(ymlDefintion, envVars).getConfig(), envVars));
	}

	public BuildConfiguration(MapValue<String, ?> config) {
		super(".ci.yml", config);
		setSections();
	}

	private void setSections() {
        this.parentTemplateSection = new ParentTemplateSection(getSectionConfig(ParentTemplateSection.NAME, StringValue.class));
		this.environmentSection = new EnvironmentSection(getSectionConfig(EnvironmentSection.NAME, MapValue.class));
		this.buildSection = new BuildSection(this.getSectionConfig(BuildSection.NAME, MapValue.class));
		this.notificationsSection = new NotificationsSection(getSectionConfig(NotificationsSection.NAME, ListValue.class));
		this.pluginsSection = new PluginsSection(getSectionConfig(PluginsSection.NAME, ListValue.class));
		setSubSections(environmentSection, buildSection, notificationsSection, pluginsSection);
	}

	@SuppressWarnings({ "unchecked" })
	private Object readResolve() {
		OneToTwoTranslator translator = new OneToTwoTranslator(configValue.getValue(), new EnvVars("BRANCH", "unknown"));
		if (translator.needsTranslation()) {
			setConfigValue(new MapValue(translator.getConfig()));
			setSections();
		}
		return this;
	}

    public String getParentTemplate(){
        return parentTemplateSection.getConfigValue().getValue();
    }

	private static MapValue<String, ?> translateIfOne(Map config, EnvVars envVars) {
		return new MapValue<String, Object>(new OneToTwoTranslator(config, envVars).getConfig());
	}

	public boolean isSkipped() {
		return buildSection.isSkipped();
	}

	public List<PostBuildNotifier> getNotifiers() {
		return notificationsSection.getNotifiers();
	}

	public boolean isMultiLanguageVersions() {
		return environmentSection.isMultiLanguageVersions();
	}

	public boolean isMultiScript() {
		return buildSection.isMultiScript();
	}

	public List<String> getLanguageVersions() {
		return environmentSection.getLanguageVersions();
	}

	public List<DotCiPluginAdapter> getPlugins() {
		return pluginsSection.getPlugins();
	}

	public List<String> getScriptKeys() {
		return buildSection.getScriptKeys();
	}

	public String getLanguage() {
		return environmentSection.getLanguage();
	}

	public boolean isParallized() {
		return isMultiScript() || isMultiLanguageVersions();
	}

	public String toYaml() {
		return new Yaml().dump(getFinalConfigValue());
	}

	public static BuildConfiguration restoreFromYaml(String yaml) {
		MapValue<String, Object> config = new MapValue<String, Object>(new Yaml().load(yaml));
		return new BuildConfiguration(config);
	}

	public String getCheckoutScript() {
		return buildSection.getCheckoutScript();
	}

	public String getBuildRunScriptForDockerImage(String buildId, Map<String, String> envVars) {
		ShellCommands dockerCommands = environmentSection.getDockerBuildRunScriptForImage(buildId, envVars);
		return dockerCommands.toShellScript();
	}

	public String getDockerBuildRunCleanupScript(String buildId) {
		return environmentSection.getCleanupScript(buildId);
	}

	public boolean isDocker() {
		return environmentSection.isDocker();
	}

	public String getBuildRunScriptForDockerLocal(String buildId, Map<String, String> dotCiEnvVars, String buildScript) {
		ShellCommands dockerCommands = environmentSection.getDockerBuildRunScriptForLocal(buildId, dotCiEnvVars, buildScript);
		return dockerCommands.toShellScript();
	}

    public boolean isBaseTemplate() {
        return !parentTemplateSection.isSpecified();
    }
}
