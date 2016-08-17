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
import com.google.common.collect.Iterables;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.branchhistory.BranchHistoryWidget;
import com.groupon.jenkins.dynamic.build.api.DynamicProjectApi;
import com.groupon.jenkins.dynamic.buildtype.BuildType;
import com.groupon.jenkins.dynamic.buildtype.BuildTypeProperty;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainerRepository;
import com.groupon.jenkins.github.GithubRepoProperty;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import hudson.Extension;
import hudson.PermalinkList;
import hudson.matrix.Combination;
import hudson.model.*;
import hudson.model.Queue.Task;
import hudson.util.CaseInsensitiveComparator;
import hudson.util.CopyOnWriteMap;
import hudson.widgets.HistoryWidget;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.*;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

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

    public boolean shouldBuildTags() {
        return getProperty(BuildTagsProperty.class)!=null && getProperty(BuildTagsProperty.class).isShouldBuildTags();
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
    protected HistoryWidget createHistoryWidget() {
        return new BranchHistoryWidget(
            this,
            HISTORY_ADAPTER,
            SetupConfig.get().getDynamicBuildRepository(),
            getCurrentBranch()
        );
    }

    @Override
    public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
        try {
            if(useNewUi(token,req)){
                rsp.forward(this,"newUi",req);
                return null;
            }
            if ("toggleNewUI".equals(token)) {
                toggleNewUI();
                rsp.forwardToPreviousPage(req);
                return null;
            }
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private boolean useNewUi(String token, StaplerRequest req) {
        return isNewUi() &&
            (StringUtils.startsWith(token,"dotCI") || //job pages
                (NumberUtils.isNumber(token) &&(StringUtils.isEmpty(req.getRestOfPath()) || StringUtils.contains(req.getRestOfPath(), "dotCI")))); // buildpages
    }

    public String getJobUrl(){
        return "job/"+ getParent().getName() +"/job/" + getName();
    }

    public boolean isNewUi() {
        return getProperty(JobUiProperty.class)!=null && getProperty(JobUiProperty.class).isNewUi();
    }

    protected String getCurrentBranch(){
        return (String) Stapler.getCurrentRequest().getSession().getAttribute("branchView" + getName());
    }

    public void doBranchBuilds(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        String tab  = req.getRestOfPath().replace("/","");
        handleBranchTabs(tab, req);
        rsp.forwardToPreviousPage(req);
    }

    private void handleBranchTabs(String branch, StaplerRequest req) {
        if("all".equals(branch)){
            req.getSession().removeAttribute("branchView" + this.getName());
        }else{
            req.getSession().setAttribute("branchView" + this.getName(), branch);
        }
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

    public void doAddBranchTab(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        String tabRegex = req.getParameter("tabRegex");
        if(StringUtils.isBlank(tabRegex)) throw new RuntimeException("Branch Regex cannot be exmpty");
        DynamicProjectBranchTabsProperty branchTabsProperty = getProperty(DynamicProjectBranchTabsProperty.class);
        branchTabsProperty.addBranch(tabRegex);
        save();
    }
    public void doRemoveBranchTab(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        String tabRegex = req.getParameter("tabRegex");
        if(StringUtils.isBlank(tabRegex)) throw new RuntimeException("Branch Regex cannot be exmpty");
        DynamicProjectBranchTabsProperty branchTabsProperty = getProperty(DynamicProjectBranchTabsProperty.class);
        branchTabsProperty.removeBranch(tabRegex);
        save();
    }
    public void toggleNewUI() {
        JobUiProperty jobUiProperty = this.getProperty(JobUiProperty.class);
        if(jobUiProperty != null){
            jobUiProperty.toggle();
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Exported
    public DynamicProjectApi getAppData() throws IOException {
        return new DynamicProjectApi(this);
    }
    public String getColor(){
        int h = Math.abs(getFullName().hashCode());
        int r = h % 250;
        int g = h % 251;
        int b = h % 252;
        return String.format("rgb(%s,%s,%s)", r,g,b);
    }

    @Override
    public int getNextBuildNumber() {
        int number = dynamicProjectRepository.getNextBuildNumber(this);
        return number;
    }

    @Override
    public synchronized int assignBuildNumber() throws IOException {
        return dynamicProjectRepository.assignNextBuildNumber(this);
    }

    @Override
    protected synchronized void saveNextBuildNumber() throws IOException {
    }
}
