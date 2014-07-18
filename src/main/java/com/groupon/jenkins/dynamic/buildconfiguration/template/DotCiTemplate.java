/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.groupon.jenkins.dynamic.buildconfiguration.template;

import com.google.common.base.CaseFormat;
import com.groupon.jenkins.dynamic.buildconfiguration.BuildConfiguration;
import com.groupon.jenkins.dynamic.buildconfiguration.InvalidDotCiYmlException;
import hudson.EnvVars;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHRepository;


public class DotCiTemplate implements ExtensionPoint {
    private String template;
    protected static Map<String, DotCiTemplate> templates = new HashMap<String, DotCiTemplate>();

    public static ExtensionList<DotCiTemplate> all() {
        return Jenkins.getInstance().getExtensionList(DotCiTemplate.class);
    }


    public static DotCiTemplate getTemplate(String templateName, GHRepository githubRepository) {
        for (DotCiTemplate template : all()) {
            if (templateName.equals(template.getName())) return template;
        }
        throw new InvalidDotCiYmlException("No Template with name: " + templateName);
    }

    private String getName() {
        String className = getClass().getSimpleName();
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className);
    }

    public static BuildConfiguration getMergedTemplate(BuildConfiguration configuration, String parentTemplate, EnvVars envVars) {
        BuildConfiguration parentBuildConfiguration = templates.get(parentTemplate).getBuildConfiguration(envVars);
        parentBuildConfiguration.merge(configuration);
        return parentBuildConfiguration;
    }

    public BuildConfiguration getBuildConfiguration(EnvVars envVars) {
        BuildConfiguration buildConfiguration = new BuildConfiguration(template, envVars);
        if (!buildConfiguration.isBaseTemplate()) {
            return DotCiTemplate.getMergedTemplate(buildConfiguration, buildConfiguration.getParentTemplate(), envVars);
        }
        return buildConfiguration;
    }


    public static void loadTemplates() {
        for (DotCiTemplate template : all()) {
            template.load();
            templates.put(template.getName(), template);
        }
    }

    private String readFile(String ymlResource) {
        InputStream base = null;
        try {
            base = this.getClass().getResourceAsStream(ymlResource);
            return IOUtils.toString(base);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(base);
        }
    }

    private void load() {
        this.template = readFile(getTemplateFile());
    }

    private String getTemplateFile() {
        Class<?> clazz = getClass();
        while (clazz != Object.class && clazz != null) {
            String name = clazz.getName().replace('.', '/').replace('$', '/') + "/" + ".ci.yml";
            if (clazz.getClassLoader().getResource(name) != null)
                return '/' + name;
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
