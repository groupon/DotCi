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

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.util.GReflectionUtils;
import com.mongodb.DBObject;
import hudson.model.*;
import hudson.model.Queue.Item;
import hudson.scm.PollingResult;
import hudson.search.QuickSilver;
import hudson.util.RunList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import com.google.common.base.Objects;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.mapping.Mapper;

@Entity("job")
public abstract class DbBackedProject<P extends DbBackedProject<P, B>, B extends DbBackedBuild<P, B>> extends Project<P, B> {

    @Id
    private ObjectId id;

    @PrePersist
    private void saveName(final DBObject dbObj) {
        dbObj.put("name", getName());
    }

    @PostLoad
    private void restoreName(final DBObject dbObj) {
        GReflectionUtils.setField(AbstractItem.class, "name", this, dbObj.get("name"));
    }

    protected transient DynamicProjectRepository dynamicProjectRepository;
    protected transient DynamicBuildRepository dynamicBuildRepository;

    private static final Logger LOGGER = Logger.getLogger(DbBackedProject.class.getName());

    public DbBackedProject(ItemGroup parent, String name) {
        super(parent, name);
        id = new ObjectId();
        initRepos();
    }

    @Override
    public void onLoad(ItemGroup<? extends hudson.model.Item> parent, String name) throws IOException {
        initRepos();
        super.onLoad(parent, name);
    }

    @PostLoad
    protected void initRepos() {
        this.dynamicProjectRepository = SetupConfig.get().getDynamicProjectRepository();
        this.dynamicBuildRepository = SetupConfig.get().getDynamicBuildRepository();
    }

    @Override
    public HealthReport getBuildHealth() {
        return new HealthReport();
    }

    @Override
    @Exported(name = "healthReport")
    public List<HealthReport> getBuildHealthReports() {
        return new ArrayList<HealthReport>();
    }

    @Override
    public synchronized void save() throws IOException {
        this.id = dynamicProjectRepository.saveOrUpdate(this);
    }

    public IdentifableItemGroup getIdentifableParent() {
        return (IdentifableItemGroup) getParent();
    }

    @Override
    public PollingResult poll(TaskListener listener) {
        return PollingResult.NO_CHANGES;
    }

    @Override
    public boolean isBuildable() {
        return !isDisabled();
    }

    // Store builds in Db

    @Override
    protected synchronized B newBuild() throws IOException {
        B build = super.newBuild();
        build.save();
        return build;
    }

    @Override
    public B getBuild(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public B getBuildByNumber(int n) {
        return dynamicBuildRepository.<B> getBuild(this, n);
    }

    @Override
    @Exported
    @QuickSilver
    public B getLastSuccessfulBuild() {
        return dynamicBuildRepository.<B> getLastSuccessfulBuild(this);
    }

    @Override
    @Exported
    @QuickSilver
    public B getLastFailedBuild() {
        return dynamicBuildRepository.<B> getLastFailedBuild(this);
    }

    @Override
    @Exported(name = "allBuilds", visibility = -2)
    @WithBridgeMethods(List.class)
    public RunList<B> getBuilds() {
        return dynamicBuildRepository.<B> getBuilds(this);
    }

    @Override
    public SortedMap<Integer, B> getBuildsAsMap() {
        return dynamicBuildRepository.<P, B> getBuildsAsMap(this);
    }

    @Override
    public B getFirstBuild() {
        return dynamicBuildRepository.<B> getFirstBuild(this);
    }

    public B getLastBuildAnyBranch() {
        return dynamicBuildRepository.<B> getLastBuild(this);
    }
    @Override
    public B getLastBuild() {
        String branch = "master";
        StaplerRequest currentRequest = Stapler.getCurrentRequest();
        if (currentRequest != null && StringUtils.isNotEmpty(currentRequest.getParameter("branch"))) {
            branch = currentRequest.getParameter("branch");
        }
        return dynamicBuildRepository.<B> getLastBuild(this,branch);
    }

    @Override
    public B getNearestBuild(int n) {
        return null;
    }

    @Override
    public B getNearestOldBuild(int n) {
        return null;
    }

    @Override
    public synchronized Item getQueueItem() {
        Queue queue = Jenkins.getInstance().getQueue();
        Item[] items = queue.getItems();
        for (int i = 0; i < items.length; i++) {
            if (items[i].task != null && items[i].task.equals(this)) {
                return items[i];
            }
        }
        return super.getQueueItem();
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        DbBackedProject other = (DbBackedProject) obj;
        return Objects.equal(getFullName(), other.getFullName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getFullName());
    }

    @Override
    public void removeRun(B run) {
        dynamicBuildRepository.deleteBuild(run);
    }

    public String getOrgName() {
        return getParent().getFullDisplayName();
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getDefaultBranch() {
        //Default to master for now; TODO: make this configurable
        return "master";
    }
    @Override
    public long getEstimatedDuration() {
        return -1;
    }
}
