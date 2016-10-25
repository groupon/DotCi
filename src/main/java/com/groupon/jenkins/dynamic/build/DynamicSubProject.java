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

import hudson.matrix.Combination;
import hudson.model.AbstractItem;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.DependencyGraph;
import hudson.model.Descriptor;
import hudson.model.Executor;
import hudson.model.InvisibleAction;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.JobProperty;
import hudson.model.Label;
import hudson.model.ParametersAction;
import hudson.model.Queue.NonBlockingTask;
import hudson.model.Queue.QueueAction;
import hudson.model.SCMedItem;
import hudson.scm.SCM;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.util.DescribableList;
import hudson.widgets.BuildHistoryWidget;
import hudson.widgets.HistoryWidget;
import jenkins.model.Jenkins;
import jenkins.scm.SCMCheckoutStrategy;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.PrePersist;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

public class DynamicSubProject extends DbBackedProject<DynamicSubProject, DynamicSubBuild> implements SCMedItem, NonBlockingTask {
    private static final Logger LOGGER = Logger.getLogger(DbBackedProject.class.getName());
    private ObjectId parentId;
    private Combination combination;

    protected DynamicSubProject(final DynamicProject parent, final String name) {
        super(parent, name);
    }

    public DynamicSubProject(final DynamicProject parent, final Combination combination) {
        this(parent, combination.toString());
        this.combination = combination;
    }

    @PrePersist
    void saveProjectId() {
        this.parentId = getParent().getId();
    }

    @Override
    public boolean isConcurrentBuild() {
        return getParent().isConcurrentBuild();
    }

    @Override
    public void setConcurrentBuild(final boolean b) throws IOException {
        throw new UnsupportedOperationException("The setting can be only changed at MatrixProject");
    }


    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public DynamicProject getParent() {
        return (DynamicProject) super.getParent();
    }

    @Override
    public int getQuietPeriod() {
        return 0;
    }

    @Override
    public int getScmCheckoutRetryCount() {
        return getParent().getScmCheckoutRetryCount();
    }

    /**
     * Inherit the value from the parent.
     */
    @Override
    public SCMCheckoutStrategy getScmCheckoutStrategy() {
        return getParent().getScmCheckoutStrategy();
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    protected Class<DynamicSubBuild> getBuildClass() {
        return DynamicSubBuild.class;
    }

    @Override
    protected HistoryWidget createHistoryWidget() {
        return new BuildHistoryWidget(this, getBuilds(), HISTORY_ADAPTER);
    }

    @Override
    protected DynamicSubBuild newBuild() throws IOException {
        final List<Action> actions = Executor.currentExecutor().getCurrentWorkUnit().context.actions;
        DynamicBuild parentBuild = getParent().getLastBuild();
        CauseAction causeAction = null;
        for (final Action a : actions) {
            if (a instanceof ParentBuildAction) {
                parentBuild = ((ParentBuildAction) a).getParent();
            }
            if (a instanceof CauseAction) {
                causeAction = (CauseAction) a;
            }

        }

        final DynamicSubBuild newBuild = new DynamicSubBuild(this, parentBuild.getCause(), parentBuild.getNumber());
        newBuild.save();
        return newBuild;
    }

    @Override
    protected void buildDependencyGraph(final DependencyGraph graph) {
    }

    @Override
    public DynamicSubProject asProject() {
        return this;
    }

    @Override
    public List<Builder> getBuilders() {
        return getParent().getBuilders();
    }

    @Override
    public DescribableList<Builder, Descriptor<Builder>> getBuildersList() {
        return getParent().getBuildersList();
    }

    @Override
    public Map<Descriptor<BuildWrapper>, BuildWrapper> getBuildWrappers() {
        return getParent().getBuildWrappers();
    }

    @Override
    public DescribableList<BuildWrapper, Descriptor<BuildWrapper>> getBuildWrappersList() {
        return getParent().getBuildWrappersList();
    }

    @Override
    public Label getAssignedLabel() {
        return getParent().getAssignedLabel();
    }


    @Override
    public SCM getScm() {
        return getParent().getScm();
    }

    public boolean scheduleBuild(final ParametersAction parameters, final Cause c) {

        return scheduleBuild(Collections.singletonList(parameters), c);
    }

    public boolean scheduleBuild(final List<? extends Action> actions, final Cause c) {
        final List<Action> allActions = new ArrayList<>();
        if (actions != null) {
            allActions.addAll(actions);
        }

        allActions.add(new CauseAction(c));

        return Jenkins.getInstance().getQueue().schedule(this, getQuietPeriod(), allActions) != null;
    }

    public Combination getCombination() {
        return this.combination;
    }

    public void setCombination(final Combination combination) {
        this.combination = combination;
    }

    @Override
    public void onLoad(final ItemGroup<? extends Item> parent, final String name) throws IOException {
        try {
            final Field parentField = AbstractItem.class.getDeclaredField("parent");
            parentField.setAccessible(true);
            ReflectionUtils.setField(parentField, this, parent);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        doSetName(name);
        if (this.transientActions == null) {
            this.transientActions = new Vector<>();
        }
        updateTransientActions();
        getBuildersList().setOwner(this);
        getPublishersList().setOwner(this);
        getBuildWrappersList().setOwner(this);

        initRepos();
    }

    public CurrentBuildState getCurrentStateByNumber(final int number) {
        return this.dynamicBuildRepository.getCurrentStateByNumber(this, number);
    }

    @Override
    public int getNextBuildNumber() {
        return 0;
    }

    @Override
    public synchronized int assignBuildNumber() throws IOException {
        return 0;
    }

    @Override
    protected synchronized void saveNextBuildNumber() throws IOException {
    }

    @Override
    public <T extends JobProperty> T getProperty(final Class<T> clazz) {
        return getParent().getProperty(clazz);
    }

    public static class ParentBuildAction extends InvisibleAction implements QueueAction {
        private final transient DynamicBuild parentBuild;

        public ParentBuildAction(final DynamicBuild parentBuild) {
            super();
            this.parentBuild = parentBuild;
        }

        @Override
        public boolean shouldSchedule(final List<Action> actions) {
            return true;
        }

        public DynamicBuild getParent() {
            return this.parentBuild;
        }
    }
}
