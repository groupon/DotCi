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

package com.groupon.jenkins.dynamic;

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.util.JsonResponse;
import hudson.Extension;
import hudson.model.Result;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import net.sf.json.JSONSerializer;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.*;

@Extension
public class RecentProjects implements RootAction{
    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "recentProjects";
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException {
        rsp.setContentType("application/json");
        Map<DynamicProject, List<DynamicBuild>> recentProjects = getRecentProjects();
        List<Map<String, Object>> output = new ArrayList<Map<String, Object>>();
        for(DynamicProject project: recentProjects.keySet()){
            HashMap<String, Object> projectMap = new HashMap<String, Object>();
            projectMap.put("name",project.getFullName());
            projectMap.put("url",project.getAbsoluteUrl());
            DynamicBuild lastBuild = project.getLastBuild();
            if(lastBuild!=null){
                projectMap.put("lastBuildStatus",lastBuild.getResult().toString());
            }else{
                projectMap.put("lastBuildStatus", Result.ABORTED.toString());
            }
            output.add(projectMap);
        }
        JsonResponse.render(rsp,output);

    }
    public Map<DynamicProject, List<DynamicBuild>> getRecentProjects(){
        Map<DynamicProject,List<DynamicBuild>> recentProjects = new HashMap<DynamicProject, List<DynamicBuild>>();
        Iterable<DynamicBuild> builds = getDynamicBuildRepository().getLastBuildsForUser(getCurrentUser(), 20);
        for(DynamicBuild build : builds){
            if(recentProjects.containsKey(build.getParent())){
                List<DynamicBuild> parentBuilds = recentProjects.get(build.getParent());
                parentBuilds.add(build);
            }else{
                ArrayList<DynamicBuild> parentBuilds = new ArrayList<DynamicBuild>();
                parentBuilds.add(build);
                recentProjects.put(build.getParent(),parentBuilds);
            }
        }
        return recentProjects;
    }

    private DynamicBuildRepository getDynamicBuildRepository() {
        return SetupConfig.get().getDynamicBuildRepository();
    }

    private String getCurrentUser() {
        return Jenkins.getAuthentication().getName();
    }
}
