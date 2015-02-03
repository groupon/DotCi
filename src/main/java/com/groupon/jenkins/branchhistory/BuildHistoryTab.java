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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicProjectBranchTabsProperty;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import hudson.model.Queue;
import jenkins.model.Jenkins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BuildHistoryTab extends HistoryTab {
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

    public Iterable<BuildHistoryRow> getBuilds() {
        String branch = getUrl().equals("all")?null:getUrl();
        Iterable<BuildHistoryRow> processedBuilds = Iterables.transform(filterSkipped(isMyBuilds() ? getDynamicBuildRepository().<DynamicBuild>getCurrentUserBuilds(project, JobHistoryWidget.BUILD_COUNT) : getDynamicBuildRepository().<DynamicBuild>getLast(project, JobHistoryWidget.BUILD_COUNT, branch)), getBuildTransformer());
        return Iterables.concat(getQueuedBuilds(),processedBuilds);
    }

    private Iterable<BuildHistoryRow> getQueuedBuilds() {
        ArrayList<BuildHistoryRow> queuedBuilds = new ArrayList<BuildHistoryRow>();
        final List<Queue.Item> queuedItems = getQueuedItems();
        for(int i=0 ; i < queuedItems.size(); i++){
            int number = queuedItems.size() == 1 ? project.getNextBuildNumber() : project.getNextBuildNumber() + queuedItems.size() - i - 1;
            queuedBuilds.add(new QueuedBuildHistoryRow(queuedItems.get(i),number));
        }
        return  queuedBuilds;
    }

    private Function<DynamicBuild, BuildHistoryRow> getBuildTransformer() {
        return new Function<DynamicBuild, BuildHistoryRow>() {
            @Override
            public BuildHistoryRow apply(DynamicBuild dynamicBuild) {
                return new ProcessedBuildHistoryRow(dynamicBuild);
            }
        };
    }

    private Iterable<DynamicBuild> filterSkipped(Iterable<DynamicBuild> builds) {
        return Iterables.filter(builds, new Predicate<DynamicBuild>() {
            @Override
            public boolean apply(DynamicBuild build) {
                return !build.isSkipped();
            }
        });
    }

    public String getUrl(){
        return url;
    }
    public String getFontIcon(){
        return font;
    }

    public String getState(){
        return state;
    }
    public String getName(){
        return name;
    }
    @Override
    public boolean isRemovable(){
        return removable;
    }

    private static Iterable<HistoryTab> getTabs(List<String> branches,DynamicProject project) {
        ArrayList<HistoryTab> tabs = new ArrayList<HistoryTab>();
        tabs.add(getAll(project));
        tabs.add(getMine(project));
        for(String branch:branches){
            tabs.add(getBranch(branch,project));
        }
        return tabs;
    }



    public static Iterable<HistoryTab> getTabs(DynamicProject project) {
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

    public List<Queue.Item> getQueuedItems() {
        LinkedList<Queue.Item> list = new LinkedList<Queue.Item>();
        for (Queue.Item item : Jenkins.getInstance().getQueue().getApproximateItemsQuickly()) {
            if (item.task == project) {
                list.addFirst(item);
            }
        }
        return list;
    }
    //    public void doAjax(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
//        req.getView(this, "ajax_build_history.jelly").forward(req, rsp);
//    }
//
//    public Iterable<DynamicBuild> getAjaxList() {
//        StaplerRequest req = Stapler.getCurrentRequest();
//        int firstBuildNumber = Integer.parseInt(req.getParameter("firstBuildNumber"));
//        int lastBuildNumber = Integer.parseInt(req.getParameter("lastBuildNumber"));
//        return model.getBuildsInProgress(firstBuildNumber, lastBuildNumber);
//    }
}
