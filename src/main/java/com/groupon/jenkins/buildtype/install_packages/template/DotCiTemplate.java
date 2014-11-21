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

package com.groupon.jenkins.buildtype.install_packages.template;

import com.google.common.base.CaseFormat;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.BuildConfiguration;
import com.groupon.jenkins.util.ResourceUtils;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.HashMap;
import java.util.Map;
import jenkins.model.Jenkins;
import org.kohsuke.github.GHRepository;


public class DotCiTemplate implements ExtensionPoint {
    private String ymlDefintion;
    private static Map<String, DotCiTemplate> templates;

    public static ExtensionList<DotCiTemplate> all() {
        return Jenkins.getInstance().getExtensionList(DotCiTemplate.class);
    }


    private String getName() {
        String className = getClass().getSimpleName();
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className);
    }

    public  BuildConfiguration getMergedTemplate(BuildConfiguration configuration, String parentTemplate, Map<String,Object> envVars) {
         return getMergedTemplate(configuration,getTemplates().get(parentTemplate),envVars);
    }
    public  BuildConfiguration getMergedTemplate(BuildConfiguration configuration, DotCiTemplate parentTemplate, Map<String,Object> envVars) {
        BuildConfiguration parentBuildConfiguration = parentTemplate.getBuildConfiguration(envVars);
        parentBuildConfiguration.merge(configuration);
        return parentBuildConfiguration;
    }

     Map<String, DotCiTemplate>  getTemplates() {
        if(templates == null){
            loadTemplates();
        }
       return templates;
    }

    public BuildConfiguration getBuildConfiguration(Map<String,Object> envVars) {
        BuildConfiguration buildConfiguration = new BuildConfiguration(ymlDefintion, envVars);
        if (!buildConfiguration.isBaseTemplate()) {
            return getMergedTemplate(buildConfiguration, buildConfiguration.getParentTemplate(), envVars);
        }
        return buildConfiguration;
    }


    public synchronized void loadTemplates() {
        templates = new HashMap<String, DotCiTemplate>();
        for (DotCiTemplate template : all()) {
            template.load();
            templates.put(template.getName(), template);
        }
    }


    private void load() {
        this.ymlDefintion = ResourceUtils.readResource(getClass(),".ci.yml");
    }



    public  DotCiTemplate getDefaultFor(GHRepository githubRepository) {
        for(DotCiTemplate template : getTemplates().values()){
           if(template.isDefault(githubRepository)) return template;
        }
        return null;
    }

    protected boolean isDefault(GHRepository githubRepository) {
        return false;
    }
}
