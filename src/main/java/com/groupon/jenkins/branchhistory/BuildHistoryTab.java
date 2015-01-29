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

package com.groupon.jenkins.branchhistory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicProjectBranchTabsProperty;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildHistoryTab {

    private boolean active;
    private String url;
    private String font;
    private String state;
    private String name;
    private boolean removable;
    private DynamicProject project;

    private static BuildHistoryTab getAll(DynamicProject project){
        return new BuildHistoryTab("all","fa fa-users","grey","All", false,project);
    }

    private static BuildHistoryTab getMine(DynamicProject project){
        return new BuildHistoryTab("mine","octicon octicon-person","grey","mine", false,project);
    }

    private static BuildHistoryTab getBranch(String branch, DynamicProject project){
        return new BuildHistoryTab(branch,"octicon octicon-git-branch","grey",branch, true,project);
    }
    public BuildHistoryTab(String url, String font, String state, String name, boolean removable, DynamicProject project) {
        this.url = url;
        this.font = font;
        this.state = state;
        this.name = name;
        this.removable = removable;
        this.project = project;
    }
    private DynamicBuildRepository getDynamicBuildRepository(){
        return SetupConfig.get().getDynamicBuildRepository();
    }

    public Iterable<DynamicBuild> getBuilds() {
        String branch = getUrl().equals("all")?null:getUrl();
        return filterSkipped(isMyBuilds() ? getDynamicBuildRepository().<DynamicBuild>getCurrentUserBuilds(project, BranchHistoryWidget.BUILD_COUNT) : getDynamicBuildRepository().<DynamicBuild>getLast(project, BranchHistoryWidget.BUILD_COUNT, branch));
    }

    private Iterable<DynamicBuild> filterSkipped(Iterable<DynamicBuild> builds) {
        return Iterables.filter(builds, new Predicate<DynamicBuild>() {
            @Override
            public boolean apply(DynamicBuild build) {
                return !build.isSkipped();
            }
        });
    }
    public boolean isActive(){
        return active;
    }
    public String getUrl(){
        return url;
    }
    public String getFont(){
        return font;
    }

    public String getState(){
        return state;
    }
    public String getName(){
        return name;
    }
    public boolean isRemovable(){
        return removable;
    }

    private static Iterable<BuildHistoryTab> getTabs(List<String> branches,DynamicProject project) {
        ArrayList<BuildHistoryTab> tabs = new ArrayList<BuildHistoryTab>();
        tabs.add(getAll(project));
        tabs.add(getMine(project));
        for(String branch:branches){
            tabs.add(getBranch(branch,project));
        }
        return tabs;
    }

    public void setActive() {
       this.active =true;
    }

    public static Iterable<BuildHistoryTab> getTabs(DynamicProject project) {
        DynamicProjectBranchTabsProperty tabsProperty = getTabsProperty(project);
        List<String> branches = tabsProperty == null ? Collections.<String>emptyList() : tabsProperty.getBranches();
        return getTabs(branches,project);
    }
    private static DynamicProjectBranchTabsProperty getTabsProperty(DynamicProject project) {
        return  project.getProperty(DynamicProjectBranchTabsProperty.class);
    }

    public boolean isMyBuilds() {
        return getName().equals("mine");
    }
}
