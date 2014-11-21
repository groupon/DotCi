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

import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListValue;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.MapValue;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.StringValue;
import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import com.groupon.jenkins.util.GroovyYamlTemplateProcessor;
import java.util.List;
import java.util.Map;

public class BuildConfiguration extends CompositeConfigSection {

    private EnvironmentSection environmentSection;
    protected BuildSection buildSection;
    private NotificationsSection notificationsSection;
    private PluginsSection pluginsSection;
    private ParentTemplateSection parentTemplateSection;

    public BuildConfiguration(String ymlDefintion, Map<String,Object> envVars) {
        this( new MapValue<String,Object> (new GroovyYamlTemplateProcessor(ymlDefintion, envVars).getConfig()));
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

    public String getParentTemplate(){
        return parentTemplateSection.getConfigValue().getValue();
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

    public boolean isBaseTemplate() {
        return !parentTemplateSection.isSpecified();
    }
}
