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
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicProjectBranchTabsProperty;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.*;
import java.util.concurrent.TimeUnit;

import  static com.google.common.collect.ImmutableMap.of;

@ExportedBean
public class JobInfo extends  ApiModel{

    private DynamicProject dynamicProject;

    public JobInfo(DynamicProject dynamicProject) {

        this.dynamicProject = dynamicProject;
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
        return of("configure",dynamicProject.hasPermission(DynamicProject.CONFIGURE),
                "build", dynamicProject.hasPermission(DynamicProject.BUILD)) ;
    }
    @Exported
    public Iterable<String> getBuildHistoryTabs(){
        DynamicProjectBranchTabsProperty tabsProperty =dynamicProject.getProperty(DynamicProjectBranchTabsProperty.class);
        return tabsProperty == null ? Collections.<String>emptyList() : tabsProperty.getBranches();
    }
    @Exported
    public List<BuildHistoryRow> getBuilds(){
        return Lists.newArrayList( new BuildHistory(dynamicProject).getBuilds("master"));
    }
    @Exported
    public List<BuildTime> getBuildTimes(){
            List<BuildTime> buildTimes = new ArrayList<BuildTime>();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            List<DbBackedBuild> builds = SetupConfig.get().getDynamicBuildRepository().getSuccessfulBuilds(dynamicProject, "master", cal, Calendar.getInstance());;

            for(DbBackedBuild build : builds){
                BuildTime buildTime = new BuildTime(build.getNumber(),TimeUnit.MILLISECONDS.toMinutes(build.getDuration()));
                buildTimes.add(buildTime);
            }

        return buildTimes;
    }

}
