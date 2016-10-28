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
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MyBuildsView extends AuthenticatedView {
    @DataBoundConstructor
    public MyBuildsView(final String name) {
        super(name);
    }

    @Override
    public List<Computer> getComputers() {
        return Collections.emptyList();
    }

    @Override
    public RunList getBuilds() {
        return null;
    }

    @Override
    public boolean contains(final TopLevelItem item) {
        return item.hasPermission(Job.CONFIGURE);
    }

    @Override
    public TopLevelItem doCreateItem(final StaplerRequest req, final StaplerResponse rsp) throws IOException, ServletException {
        final ItemGroup<? extends TopLevelItem> ig = getOwnerItemGroup();
        if (ig instanceof ModifiableItemGroup) {
            return ((ModifiableItemGroup<? extends TopLevelItem>) ig).doCreateItem(req, rsp);
        }
        return null;
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        final List<TopLevelItem> items = new LinkedList<>();
        for (final TopLevelItem item : getOwnerItemGroup().getItems()) {
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

    @Override
    public void onJobRenamed(final Item item, final String oldName, final String newName) {
        // noop
    }

    @Override
    protected void submit(final StaplerRequest req) throws IOException, ServletException, FormException {
        // noop
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Extension
    public static final class DescriptorImpl extends ViewDescriptor {

        @Override
        public String getDisplayName() {
            return "DotCi - LoggedIn User Builds View";
        }
    }
}
