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

package com.groupon.jenkins.dynamic.build.plugins.downstream;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.buildtype.InvalidBuildConfigurationException;
import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.git.GitUrl;
import com.groupon.jenkins.github.NoDuplicatesParameterAction;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Extension
public class DownstreamJobPlugin extends DotCiPluginAdapter {
    public DownstreamJobPlugin() {
        super("downstream_job");
    }

    @Override
    public boolean perform(DynamicBuild dynamicBuild, Launcher launcher, BuildListener listener) {
        if(!(options instanceof Map)){
           throw new InvalidBuildConfigurationException("Invalid format specified for " + getName()+" . Expecting a Map.") ;
        }
        Map<String,Object> jobOptions = (Map<String,Object>) options;
        for(String jobName: jobOptions.keySet()){
            DynamicProject job = findJob(jobName);
            job.scheduleBuild(0, getCause(dynamicBuild,job, jobOptions.get(jobName)), getParamsAction(jobOptions.get(jobName)));
        }

        return true;
    }

    private NoDuplicatesParameterAction getParamsAction(Object options) {
        Map<String, String> paramOptions = (Map<String, String>) options;
        List<ParameterValue> params = new ArrayList<ParameterValue>();
        for(Map.Entry<String, String> entry : paramOptions.entrySet()){
            params.add(new StringParameterValue(entry.getKey(),entry.getValue()));
        }

        return new NoDuplicatesParameterAction(params);
    }

    private DynamicProject findJob(String jobName) {
        final String orgName = jobName.split("/")[0];
        final String repoName = jobName.split("/")[1];
        return Iterables.find(Jenkins.getInstance().getAllItems(DynamicProject.class), new Predicate<DynamicProject>() {
            @Override
            public boolean apply(DynamicProject project) {
                return project.getParent().getName().equals(orgName) && project.getName().equals(repoName);
            }
        });
    }

    public Cause getCause(DynamicBuild sourceBuild, DynamicProject targetJob, Object jobOptions) {
        return new DotCiDownstreamCause(sourceBuild, targetJob, (Map<String, String>) jobOptions);
    }
}
