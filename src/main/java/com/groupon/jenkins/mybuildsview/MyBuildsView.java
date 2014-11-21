/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.groupon.jenkins.mybuildsview;

import com.google.common.collect.Lists;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.views.AuthenticatedView;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Descriptor.FormException;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.ModifiableItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.ViewDescriptor;
import hudson.util.RunList;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;

public class MyBuildsView extends AuthenticatedView {
    @DataBoundConstructor
    public MyBuildsView(String name) {
        super(name);
    }

    protected DynamicBuildRepository makeDynamicBuildRepository() {
        return SetupConfig.get().getDynamicBuildRepository();
    }


    @Override
    public List<Computer> getComputers() {
        return Collections.emptyList();
    }

    @Override
    public RunList getBuilds() {
        Iterable<DynamicBuild> builds = makeDynamicBuildRepository().getLastBuildsForUser(getCurrentUser(), 20);
        return RunList.fromRuns(Lists.newArrayList(builds));
    }

    private String getCurrentUser() {
        return Jenkins.getAuthentication().getName();
    }


    public void doBuilds(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        req.getSession().setAttribute("viewType", "builds");
        rsp.forwardToPreviousPage(req);
    }

    @Override
    public boolean contains(TopLevelItem item) {
        return item.hasPermission(Job.CONFIGURE);
    }

    @Override
    public TopLevelItem doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        ItemGroup<? extends TopLevelItem> ig = getOwnerItemGroup();
        if (ig instanceof ModifiableItemGroup) {
            return ((ModifiableItemGroup<? extends TopLevelItem>) ig).doCreateItem(req, rsp);
        }
        return null;
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        List<TopLevelItem> items = new LinkedList<TopLevelItem>();
        for (TopLevelItem item : getOwnerItemGroup().getItems()) {
            if (item.hasPermission(Job.CONFIGURE)) {
                items.add(item);
            }
        }
        return Collections.unmodifiableList(items);
    }

    @Override
    public String getPostConstructLandingPage() {
        return ""; // there's no configuration page
    }

    @Extension
    public static final class DescriptorImpl extends ViewDescriptor {

        @Override
        public String getDisplayName() {
            return "My Builds View";
        }
    }

    @Override
    public void onJobRenamed(Item item, String oldName, String newName) {
        // noop
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, FormException {
        // noop
    }

    public String getViewType() {
        StaplerRequest currentRequest = Stapler.getCurrentRequest();
        String viewType = (String) currentRequest.getSession().getAttribute("viewType");
        return viewType == null ? "builds" : viewType;
    }

    @Override
    public boolean isEditable() {
        return false;
    }
}
