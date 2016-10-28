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
import com.groupon.jenkins.dynamic.build.api.metrics.JobMetric;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;

@ExportedBean
public class JobInfo extends ApiModel {

    private final DynamicProject dynamicProject;

    public JobInfo(final DynamicProject dynamicProject) {
        this.dynamicProject = dynamicProject;
    }

    @Exported
    public String getFullName() {
        return this.dynamicProject.getFullName();
    }

    @Exported
    public String getGithubUrl() {
        return this.dynamicProject.getGithubRepoUrl();
    }

    @Exported
    public Map getPermissions() {
        return of("configure", this.dynamicProject.hasPermission(DynamicProject.CONFIGURE),
            "build", this.dynamicProject.hasPermission(DynamicProject.BUILD));
    }

    @Exported
    public Iterable<String> getBuildHistoryTabs() {
        final DynamicProjectBranchTabsProperty tabsProperty = this.dynamicProject.getProperty(DynamicProjectBranchTabsProperty.class);
        final List<String> tabs = tabsProperty == null ? new ArrayList<>() : tabsProperty.getBranches();
        final ArrayList<String> configuredTabs = new ArrayList<>();
        if (!tabs.contains("Mine")) configuredTabs.add("Mine");
        if (!tabs.contains("All")) configuredTabs.add("All");
        configuredTabs.addAll(tabs);
        return configuredTabs;
    }

    @Exported
    public List<Build> getBuilds() {
        final StaplerRequest req = Stapler.getCurrent().getCurrentRequest();
        final String branchTab = req.getParameter("branchTab");
        final String buildCount = req.getParameter("count");
        return Lists.newArrayList(new BuildHistory(this.dynamicProject).getBuilds(branchTab, Integer.parseInt(buildCount)));
    }

    @Exported
    public List<JobMetric> getMetrics() {
        final StaplerRequest req = Stapler.getCurrent().getCurrentRequest();
        final String branchTab = req.getParameter("branchTab");
        final String buildCount = req.getParameter("count");
        return JobMetric.getApplicableJobMetrics(this.dynamicProject, branchTab, Integer.parseInt(buildCount));
    }
}
