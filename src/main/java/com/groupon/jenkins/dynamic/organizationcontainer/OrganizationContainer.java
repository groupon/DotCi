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
package com.groupon.jenkins.dynamic.organizationcontainer;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.IdentifableItemGroup;
import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.DescriptorVisibilityFilter;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.ItemGroupMixIn;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.View;
import hudson.model.ViewGroup;
import hudson.model.ViewGroupMixIn;
import hudson.util.CaseInsensitiveComparator;
import hudson.util.CopyOnWriteMap;
import hudson.util.CopyOnWriteMap.Tree;
import hudson.views.DefaultViewsTabBar;
import hudson.views.ViewsTabBar;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.bson.types.ObjectId;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerFallback;
import org.kohsuke.stapler.StaplerOverridable;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class OrganizationContainer extends AbstractItem implements IdentifableItemGroup<DynamicProject>, ViewGroup, TopLevelItem, StaplerOverridable, StaplerFallback, StaplerProxy {
    private ObjectId id;
    private transient Map<String, DynamicProject> items = new CopyOnWriteMap.Tree<>(CaseInsensitiveComparator.INSTANCE);
    private transient ItemGroupMixIn mixin;

    private CopyOnWriteArrayList<View> views;
    private ViewsTabBar viewsTabBar;

    private String primaryView;

    private transient ViewGroupMixIn viewGroupMixIn;
    private transient OrganizationGravatarIcon icon;

    public OrganizationContainer(final ItemGroup parent, final String name) {
        super(parent, name);
        init(name);
        if (this.id == null) {
            this.id = new ObjectId();
        }
    }

    private void init(final String name) {
        if (this.icon == null) {
            this.icon = new OrganizationGravatarIcon(name);
        }
        this.mixin = new MixInImpl(this);
        if (this.views == null) {
            this.views = new CopyOnWriteArrayList<>();
        }
        if (this.views.size() == 0) {
            final AllListView lv = new AllListView(this);
            this.views.add(lv);
        }
        if (this.viewsTabBar == null) {
            this.viewsTabBar = new DefaultViewsTabBar();
        }
        if (this.primaryView == null) {
            this.primaryView = this.views.get(0).getViewName();
        }
        this.mixin = new MixInImpl(this);
        this.viewGroupMixIn = new ViewGroupMixIn(this) {
            @Override
            protected List<View> views() {
                return OrganizationContainer.this.views;
            }

            @Override
            protected String primaryView() {
                return OrganizationContainer.this.primaryView;
            }

            @Override
            protected void primaryView(final String name) {
                OrganizationContainer.this.primaryView = name;
            }
        };
        this.items = new CopyOnWriteMap.Tree<>(CaseInsensitiveComparator.INSTANCE);
        this.items = getJobsForThisContainer();
    }

    private Tree<String, DynamicProject> getJobsForThisContainer() {
        final Iterable<DynamicProject> projects = SetupConfig.get().getDynamicProjectRepository().getProjectsForOrg(this);
        final Tree<String, DynamicProject> itemMap = new CopyOnWriteMap.Tree<>(CaseInsensitiveComparator.INSTANCE);
        for (final DynamicProject dbBackedProject : projects) {
            itemMap.put(dbBackedProject.getName(), dbBackedProject);
            try {
                dbBackedProject.onLoad(this, dbBackedProject.getName());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        return itemMap;
    }

    public synchronized void reloadItems() {
        this.items = getJobsForThisContainer();
    }

    @Override
    public void onLoad(final ItemGroup<? extends Item> parent, final String name) throws IOException {
        super.onLoad(parent, name);
        init(name);
    }

    public DynamicProject doCreateItem(final StaplerRequest req, final StaplerResponse rsp) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<DynamicProject> getItems() {
        return this.items.values();
    }

    @Override
    public String getUrlChildPrefix() {
        return "job";
    }

    @Override
    public DynamicProject getItem(final String name) {
        return this.items.get(name);
    }

    @Override
    public File getRootDirFor(final DynamicProject child) {
        return getRootDirFor(child.getName());
    }

    private File getRootDirFor(final String name) {
        return new File(getJobsDir(), name);
    }

    private File getJobsDir() {
        return new File(getRootDir(), "jobs");
    }

    @Override
    public void onRenamed(final DynamicProject item, final String oldName, final String newName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onDeleted(final DynamicProject item) throws IOException {
        SetupConfig.get().getDynamicProjectRepository().delete(item);
        this.items.remove(item.getName());
    }

    @Override
    public Object getStaplerFallback() {
        return getPrimaryView();
    }

    @Override
    public Collection<?> getOverrides() {
        return null;
    }

    @Override
    public TopLevelItemDescriptor getDescriptor() {
        return (DescriptorImpl) Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    public DynamicProject createProject(final Class<DynamicProject> type, final String projectName) throws IOException {
        return type.cast(createProject((TopLevelItemDescriptor) Hudson.getInstance().getDescriptor(type), projectName));
    }

    public TopLevelItem createProject(final TopLevelItemDescriptor type, final String name) throws IOException {
        return createProject(type, name, true);
    }

    public TopLevelItem createProject(final TopLevelItemDescriptor type, final String name, final boolean notify) throws IOException {
        return this.mixin.createProject(type, name, notify);
    }

    public OrganizationGravatarIcon getIcon() {
        return this.icon;
    }

    public void setIcon(final OrganizationGravatarIcon icon) {
        this.icon = icon;
    }

    public OrganizationGravatarIcon getIconColor() {
        return this.icon;
    }

    public <T extends TopLevelItem> T copy(final T src, final String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    public TopLevelItem createProjectFromXML(final String name, final InputStream xml) throws IOException {
        return this.mixin.createProjectFromXML(name, xml);
    }

    @Override
    public Collection<? extends Job> getAllJobs() {
        final Set<Job> jobs = new HashSet<>();
        for (final Item i : getItems()) {
            jobs.addAll(i.getAllJobs());
        }
        return jobs;
    }

    @Override
    public boolean canDelete(final View view) {
        return false;
    }

    @Override
    public void deleteView(final View view) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<View> getViews() {
        return this.viewGroupMixIn.getViews();
    }

    @Override
    public View getView(final String name) {
        return this.viewGroupMixIn.getView(name);
    }

    @Override
    public View getPrimaryView() {
        return this.viewGroupMixIn.getPrimaryView();
    }

    @Override
    public void onViewRenamed(final View view, final String oldName, final String newName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ViewsTabBar getViewsTabBar() {
        return this.viewsTabBar;
    }

    @Override
    public ItemGroup<? extends TopLevelItem> getItemGroup() {
        return this;
    }

    @Override
    public List<Action> getViewActions() {
        return Collections.emptyList();
    }

    @Override
    public Object getTarget() {
        final StaplerRequest currentRequest = Stapler.getCurrentRequest();
        //@formatter:off
        if (!currentRequest.getRequestURI().matches(".*(api/(json|xml)).*")
            && !currentRequest.getRequestURI().contains("buildWithParameters")
            && !currentRequest.getRequestURI().contains("logTail")
            && !currentRequest.getRequestURI().contains("artifact")) {
            //@formatter:on
            authenticate();
        }

        return this;
    }

    private void authenticate() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jenkins.getInstance().getSecurityRealm().getSecurityComponents().manager.authenticate(authentication);
    }

    public void addItem(final DynamicProject project) {
        this.items.put(project.getName(), project);
    }

    @Override
    public Object getId() {
        return getName();
    }

    @Extension
    public static class DescriptorImpl extends TopLevelItemDescriptor {

        @Override
        public String getDisplayName() {
            return "Org";
        }

        @Override
        public TopLevelItem newInstance(final ItemGroup parent, final String name) {
            return new OrganizationContainer(parent, name);
        }

        @Extension
        /**
         * Cannot create this view Manually
         */
        public static class FilterOrganizationContainerProjectTypeFromNewJobPage extends DescriptorVisibilityFilter {
            @Override
            public boolean filter(final Object context, final Descriptor descriptor) {
                return !(descriptor instanceof OrganizationContainer.DescriptorImpl);
            }
        }

    }

    private class MixInImpl extends ItemGroupMixIn {
        private MixInImpl(final OrganizationContainer parent) {
            super(parent, parent);
        }

        @Override
        protected void add(final TopLevelItem item) {
            OrganizationContainer.this.items.put(item.getName(), (DynamicProject) item);
        }

        @Override
        protected File getRootDirFor(final String name) {
            return OrganizationContainer.this.getRootDirFor(name);
        }
    }

}
