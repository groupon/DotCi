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
package com.groupon.jenkins.dynamic.build;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.branchhistory.JobHistoryWidget;
import com.groupon.jenkins.dynamic.buildtype.BuildType;
import com.groupon.jenkins.dynamic.buildtype.BuildTypeProperty;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainerRepository;
import com.groupon.jenkins.github.GithubRepoProperty;
import com.groupon.jenkins.util.JsonResponse;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import hudson.Extension;
import hudson.PermalinkList;
import hudson.matrix.Combination;
import hudson.model.*;
import hudson.model.Queue.Task;
import hudson.util.CaseInsensitiveComparator;
import hudson.util.CopyOnWriteMap;

import java.io.File;
import java.io.IOException;
import java.util.*;

import hudson.widgets.Widget;
import jenkins.model.Jenkins;
import net.sf.json.JSONSerializer;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;

import javax.servlet.ServletException;
import static com.google.common.collect.ImmutableMap.of;

public class DynamicProject extends DbBackedProject<DynamicProject, DynamicBuild> implements TopLevelItem, Saveable, IdentifableItemGroup<DynamicSubProject> {
    private transient Map<String, DynamicSubProject> items;
    private String containerName;

    @PrePersist
    void saveProjectId() {
        containerName = (String) getParent().getId();
    }

    @PostLoad
    void loadParent() {
        try {
            // If it didn't load on main Jenkins start, try loading it again.
            OrganizationContainer container = new OrganizationContainerRepository().getOrganizationContainer(containerName);
            if(container != null) {
                onLoad(container, getName());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load container for project: " + containerName);
        }

    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    protected DynamicProject(ItemGroup parent, String name) {
        super(parent, name);
        init();
    }

    private void init() {
        Iterable<DynamicSubProject> projects = SetupConfig.get().getDynamicProjectRepository().getChildren(this);
        items = new CopyOnWriteMap.Tree<String, DynamicSubProject>(CaseInsensitiveComparator.INSTANCE);
        for (DynamicSubProject dbBackedProject : projects) {
            items.put(dbBackedProject.getName(), dbBackedProject);
        }
    }

    @Override
    public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
        super.onLoad(parent, name);
        init();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    public static final class DescriptorImpl extends AbstractProjectDescriptor {
        /**
         * We are hiding the "DotCI" project from "/newJob" page, because we'll
         * have our own flow for doing this ...
         */
        @Extension
        public static class FilterDotCIProjectTypeFromNewJobPage extends DescriptorVisibilityFilter {
            @Override
            public boolean filter(Object context, Descriptor descriptor) {
                return !(descriptor instanceof DynamicProject.DescriptorImpl);
            }
        }

        @Override
        public String getDisplayName() {
            return "DotCi Project";
        }

        @Override
        public TopLevelItem newInstance(ItemGroup parent, String name) {
            return new DynamicProject(parent, name);
        }

    }

    @Override
    public PermalinkList getPermalinks() {
        PermalinkList permalinks = super.getPermalinks();
        permalinks.add(new LastSuccessfulMasterPermalink());
        return permalinks;
    }


    public Iterable<BuildType> getBuildTypes(){
        return SetupConfig.get().getBuildTypes();
    }

    public String getBuildType(){
        return getProperty(BuildTypeProperty.class) == null ? null : getProperty(BuildTypeProperty.class).getBuildType();
    }


    public Iterable<String> getBranchTabs(){
        DynamicProjectBranchTabsProperty branchTabsProperty = getProperty(DynamicProjectBranchTabsProperty.class);
        return branchTabsProperty ==null? Arrays.asList("master") :branchTabsProperty.getBranches();
    }

    @Override
    @WithBridgeMethods(value = Jenkins.class, castRequired = true)
    public OrganizationContainer getParent() {
        return (OrganizationContainer) super.getParent();
    }


    @Override
    public List<Widget> getWidgets() {
        List<Widget> widgets = new ArrayList<Widget>();
        widgets.add(new JobHistoryWidget(this));
        return widgets;
    }

    @Override
    public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
        if ("sha".equals(token)) {
            String sha = req.getParameter("value");
            return dynamicBuildRepository.getBuildBySha(this, sha);
        }


        Object permalink = super.getDynamic(token, req, rsp);
        if (permalink == null) {
            DynamicSubProject item = getItem(token);
            return item;
        }

        return permalink;
    }







    @Override
    protected Class<DynamicBuild> getBuildClass() {
        return DynamicBuild.class;
    }

    @Override
    public String getUrlChildPrefix() {
        return ".";
    }

    private DynamicSubProject createNewSubProject(Combination requestedCombination) {
        DynamicSubProject project = new DynamicSubProject(this, requestedCombination);
        try {
            project.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        items.put(project.getName(), project);
        return project;
    }

    public Iterable<DynamicSubProject> getSubProjects(Iterable<Combination> subBuildCombinations) {

        return Iterables.transform(subBuildCombinations, new Function<Combination, DynamicSubProject>() {

            @Override
            public DynamicSubProject apply(final Combination requestedCombination) {
                DynamicSubProject subProject = Iterables.find(getItems(), new Predicate<DynamicSubProject>() {

                    @Override
                    public boolean apply(DynamicSubProject subProject) {
                        return requestedCombination.equals(subProject.getCombination());
                    }
                }, null);
                return subProject == null? DynamicProject.this.createNewSubProject(requestedCombination): subProject;
            }

        });
    }

    public Task getItem(Combination combination) {
        return null;
    }

    @Override
    public DynamicSubProject getItem(String name) {
        return dynamicProjectRepository.getChild(this, name);
    }

    private File getConfigurationsDir() {
        return new File(getRootDir(), "configurations");
    }

    @Override
    public File getRootDirFor(DynamicSubProject child) {
        File f = new File(getConfigurationsDir(), child.getName());
        f.getParentFile().mkdirs();
        return f;
    }

    @Override
    public Collection<DynamicSubProject> getItems() {
        return items == null ? new ArrayList<DynamicSubProject>() : this.items.values();
    }

    public String getGithubRepoUrl() {
        return getProperty(GithubRepoProperty.class) == null ? null : getProperty(GithubRepoProperty.class).getRepoUrl();
    }

    @Override
    public void onRenamed(DynamicSubProject item, String oldName, String newName) throws IOException {
        throw new IllegalStateException("Renaming not allowed outside .ci.yml");
    }

    @Override
    public void onDeleted(DynamicSubProject item) throws IOException {
        throw new IllegalStateException("Cannot delete Sub Project without deleting the parent");
    }

    @Override
    public DynamicBuild getLastBuild() {
        return super.getLastBuild();
    }

    @Override
    public void doDoDelete(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        rsp.setHeader("Location", getParent().getAbsoluteUrl());
        delete();
    }

    public void doApi(StaplerRequest req, StaplerResponse rsp) throws IOException {
        // @formatter:off
        Map jobInfo = of("githubUrl", getGithubRepoUrl(),
                         "fullName", getFullName(),
                         "permissions", of("configure", hasPermission(CONFIGURE),
                                            "build", hasPermission(BUILD)));
        // @formatter:on
        JsonResponse.render(rsp, jobInfo);
    }

}
