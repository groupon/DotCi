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

import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicProjectBranchTabsProperty;

import java.util.Collections;
import java.util.Map;
import  static com.google.common.collect.ImmutableMap.of;

public class JobInfo extends  ApiModel{

        private DynamicProject dynamicProject;

        public JobInfo(DynamicProject dynamicProject) {

            this.dynamicProject = dynamicProject;
        }

        public String getFullName(){
            return dynamicProject.getFullName();
        }
        public String getGithubUrl(){
            return dynamicProject.getGithubRepoUrl();
        }
        public Map getPermissions(){
            return of("configure",dynamicProject.hasPermission(DynamicProject.CONFIGURE),
                    "build", dynamicProject.hasPermission(DynamicProject.BUILD)) ;
        }
        public Iterable<String> getBuildHistoryTabs(){
            DynamicProjectBranchTabsProperty tabsProperty =dynamicProject.getProperty(DynamicProjectBranchTabsProperty.class);
            return tabsProperty == null ? Collections.<String>emptyList() : tabsProperty.getBranches();
        }
        public Iterable<BuildHistoryRow> getBuilds(){
           return new BuildHistory(dynamicProject).getBuilds("master");
        }
}
