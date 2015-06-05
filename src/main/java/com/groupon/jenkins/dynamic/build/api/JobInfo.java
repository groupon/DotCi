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

package com.groupon.jenkins.dynamic.build.api;

import com.google.common.collect.Lists;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicProjectBranchTabsProperty;
import com.groupon.jenkins.dynamic.build.api.metrics.BuildTimeMetric;
import com.groupon.jenkins.dynamic.build.api.metrics.JobMetric;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

import  static com.google.common.collect.ImmutableMap.of;

@ExportedBean
public class JobInfo extends  ApiModel{

    private DynamicProject dynamicProject;
    private String branchTab;
    private String buildCount;

    public JobInfo(DynamicProject dynamicProject) {
        this.dynamicProject = dynamicProject;
    }

    @Override
    public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        setBranchTab(req.getParameter("branchTab"));
        setBuildCount(req.getParameter("count"));
        super.doIndex(req, rsp);
    }

    @Exported
    public String getFullName(){
        return dynamicProject.getFullName();
    }
    @Exported
    public String getGithubUrl(){
        return dynamicProject.getGithubRepoUrl();
    }
    @Exported
    public Map getPermissions(){
        return of("configure", dynamicProject.hasPermission(DynamicProject.CONFIGURE),
                "build", dynamicProject.hasPermission(DynamicProject.BUILD)) ;
    }
    @Exported
    public Iterable<String> getBuildHistoryTabs(){
        DynamicProjectBranchTabsProperty tabsProperty =dynamicProject.getProperty(DynamicProjectBranchTabsProperty.class);
        List<String> tabs = tabsProperty == null ? new ArrayList<String>() : tabsProperty.getBranches();
        ArrayList<String> configuredTabs = new ArrayList<String>();
        if(!tabs.contains("Mine")) configuredTabs.add("Mine");
        if(!tabs.contains("All")) configuredTabs.add("All");
        configuredTabs.addAll(tabs);
        return configuredTabs;
    }
    @Exported
    public List<Build> getBuilds(){
        return Lists.newArrayList(new BuildHistory(dynamicProject).getBuilds(branchTab,Integer.parseInt(buildCount)));
    }
    @Exported
    public List<JobMetric> getMetrics(){
      return  JobMetric.getApplicableJobMetrics(dynamicProject);
    }
    public void setBranchTab(String branchTab) {
        this.branchTab = branchTab;
    }

    private String getBranchTab() {
        return branchTab;
    }

    public void setBuildCount(String buildCount) {
        this.buildCount = buildCount;
    }
}
