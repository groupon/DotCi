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

import com.google.common.base.Objects;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import com.groupon.jenkins.util.GReflectionUtils;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import com.mongodb.DBObject;
import hudson.model.AbstractItem;
import hudson.model.HealthReport;
import hudson.model.ItemGroup;
import hudson.model.Project;
import hudson.model.Queue;
import hudson.model.Queue.Item;
import hudson.model.TaskListener;
import hudson.scm.PollingResult;
import hudson.search.QuickSilver;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Logger;

@Entity("job")
public abstract class DbBackedProject<P extends DbBackedProject<P, B>, B extends DbBackedBuild<P, B>> extends Project<P, B> {

    private static final Logger LOGGER = Logger.getLogger(DbBackedProject.class.getName());
    protected transient DynamicProjectRepository dynamicProjectRepository;
    protected transient DynamicBuildRepository dynamicBuildRepository;
    @Id
    private ObjectId id;

    public DbBackedProject(final ItemGroup parent, final String name) {
        super(parent, name);
        this.id = new ObjectId();
        initRepos();
    }

    @PrePersist
    private void saveName(final DBObject dbObj) {
        dbObj.put("name", getName());
    }

    @PostLoad
    private void restoreName(final DBObject dbObj) {
        GReflectionUtils.setField(AbstractItem.class, "name", this, dbObj.get("name"));
    }

    @Override
    public void onLoad(final ItemGroup<? extends hudson.model.Item> parent, final String name) throws IOException {
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
        return new ArrayList<>();
    }

    @Override
    public synchronized void save() throws IOException {
        this.id = this.dynamicProjectRepository.saveOrUpdate(this);
    }

    public IdentifableItemGroup getIdentifableParent() {
        return (IdentifableItemGroup) getParent();
    }

    @Override
    public PollingResult poll(final TaskListener listener) {
        return PollingResult.NO_CHANGES;
    }

    @Override
    public boolean isBuildable() {
        return !isDisabled();
    }

    // Store builds in Db

    @Override
    protected synchronized B newBuild() throws IOException {
        final B build = super.newBuild();
        build.save();
        return build;
    }

    @Override
    public B getBuild(final String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public B getBuildByNumber(final int n) {
        return this.dynamicBuildRepository.<B>getBuild(this, n);
    }

    @Override
    @Exported
    @QuickSilver
    public B getLastSuccessfulBuild() {
        return this.dynamicBuildRepository.<B>getLastSuccessfulBuild(this);
    }

    @Override
    @Exported
    @QuickSilver
    public B getLastFailedBuild() {
        return this.dynamicBuildRepository.<B>getLastFailedBuild(this);
    }

    @Override
    @Exported(name = "allBuilds", visibility = -2)
    @WithBridgeMethods(List.class)
    public RunList<B> getBuilds() {
        return this.dynamicBuildRepository.<B>getBuilds(this);
    }

    @Override
    public SortedMap<Integer, B> getBuildsAsMap() {
        return this.dynamicBuildRepository.<P, B>getBuildsAsMap(this);
    }

    @Override
    public B getFirstBuild() {
        return this.dynamicBuildRepository.<B>getFirstBuild(this);
    }

    public B getLastBuildAnyBranch() {
        return this.dynamicBuildRepository.<B>getLastBuild(this);
    }

    @Override
    public B getLastBuild() {
        String branch = "master";
        final StaplerRequest currentRequest = Stapler.getCurrentRequest();
        if (currentRequest != null && StringUtils.isNotEmpty(currentRequest.getParameter("branch"))) {
            branch = currentRequest.getParameter("branch");
        }
        return this.dynamicBuildRepository.<B>getLastBuild(this, branch);
    }

    @Override
    public B getNearestBuild(final int n) {
        return null;
    }

    @Override
    public B getNearestOldBuild(final int n) {
        return null;
    }

    @Override
    public synchronized Item getQueueItem() {
        final Queue queue = Jenkins.getInstance().getQueue();
        final Item[] items = queue.getItems();
        for (int i = 0; i < items.length; i++) {
            if (items[i].task != null && items[i].task.equals(this)) {
                return items[i];
            }
        }
        return super.getQueueItem();
    }

    @Override
    public boolean equals(final Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DbBackedProject other = (DbBackedProject) obj;
        return Objects.equal(getFullName(), other.getFullName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getFullName());
    }

    @Override
    public void removeRun(final B run) {
        this.dynamicBuildRepository.deleteBuild(run);
    }

    public String getOrgName() {
        return getParent().getFullDisplayName();
    }

    public ObjectId getId() {
        return this.id;
    }

    public void setId(final ObjectId id) {
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

    public B getNextBuild(final int number) {
        return this.dynamicBuildRepository.getNextBuild(this, number);
    }
}
