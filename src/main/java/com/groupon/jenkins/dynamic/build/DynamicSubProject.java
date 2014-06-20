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
import hudson.model.Action;
import hudson.model.DependencyGraph;
import hudson.model.InvisibleAction;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.SCMedItem;
import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Descriptor;
import hudson.model.Executor;
import hudson.model.Label;
import hudson.model.ParametersAction;
import hudson.model.Queue.NonBlockingTask;
import hudson.model.Queue.QueueAction;
import hudson.scm.SCM;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.tasks.LogRotator;
import hudson.util.DescribableList;
import hudson.widgets.BuildHistoryWidget;
import hudson.widgets.HistoryWidget;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jenkins.model.Jenkins;
import jenkins.scm.SCMCheckoutStrategy;

import org.springframework.util.ReflectionUtils;

public class DynamicSubProject extends DbBackedProject<DynamicSubProject, DynamicSubBuild> implements SCMedItem, NonBlockingTask {

	private Combination combination;

	protected DynamicSubProject(DynamicProject parent, String name) {
		super(parent, name);
	}

	public DynamicSubProject(DynamicProject parent, Combination combination) {
		this(parent, combination.toString());
		this.combination = combination;
	}

	@Override
	public boolean isConcurrentBuild() {
		return getParent().isConcurrentBuild();
	}

	@Override
	public void setConcurrentBuild(boolean b) throws IOException {
		throw new UnsupportedOperationException("The setting can be only changed at MatrixProject");
	}

	@Override
	public int getNextBuildNumber() {
		AbstractBuild<?, ?> lb = getParent().getLastBuildAnyBranch();
		int n = lb.getNumber() + 1;
		return n;
	}

	@Override
	public int assignBuildNumber() throws IOException {
		int nb = getNextBuildNumber();
		DynamicSubBuild r = getLastBuild();
		if (r != null && r.getNumber() >= nb) {
			// make sure we don't schedule the same build twice
			throw new IllegalStateException("Build #" + nb + " is already completed");
		}
		return nb;
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
		List<Action> actions = Executor.currentExecutor().getCurrentWorkUnit().context.actions;
		DynamicBuild parentBuild = getParent().getLastBuild();
		CauseAction causeAction = null;
		for (Action a : actions) {
			if (a instanceof ParentBuildAction) {
				parentBuild = ((ParentBuildAction) a).parent;
			}
			if (a instanceof CauseAction) {
				causeAction = (CauseAction) a;
			}

		}

		DynamicSubBuild newBuild = new DynamicSubBuild(this, parentBuild.getTimestamp(), parentBuild.getCause());

		newBuild.number = parentBuild.getNumber();
		newBuild.save();
		return newBuild;
	}

	@Override
	protected void buildDependencyGraph(DependencyGraph graph) {
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
	public LogRotator getLogRotator() {
		LogRotator lr = getParent().getLogRotator();
		int artifactDaysToKeep = lr != null ? lr.getArtifactDaysToKeep() : -1;
		int artifactNumToKeep = lr != null ? lr.getArtifactNumToKeep() : -1;
		return new LinkedLogRotator(artifactDaysToKeep, artifactNumToKeep);
	}

	@Override
	public SCM getScm() {
		return getParent().getScm();
	}

	public boolean scheduleBuild(ParametersAction parameters, Cause c) {

		return scheduleBuild(Collections.singletonList(parameters), c);
	}

	public boolean scheduleBuild(List<? extends Action> actions, Cause c) {
		List<Action> allActions = new ArrayList<Action>();
		if (actions != null) {
			allActions.addAll(actions);
		}
		allActions.add(new ParentBuildAction());
		allActions.add(new CauseAction(c));

		return Jenkins.getInstance().getQueue().schedule(this, getQuietPeriod(), allActions) != null;
	}

	public static class ParentBuildAction extends InvisibleAction implements QueueAction {
		public transient DynamicBuild parent = (DynamicBuild) Executor.currentExecutor().getCurrentExecutable();

		@Override
		public boolean shouldSchedule(List<Action> actions) {
			return true;
		}
	}

	public Combination getCombination() {
		return combination;
	}

	public void setCombination(Combination combination) {
		this.combination = combination;
	}

	@Override
	public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
		try {
			Field parentField = AbstractItem.class.getDeclaredField("parent");
			parentField.setAccessible(true);
			ReflectionUtils.setField(parentField, this, parent);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		doSetName(name);
		if (transientActions == null) {
			transientActions = new Vector<Action>();
		}
		updateTransientActions();
		getBuildersList().setOwner(this);
		getPublishersList().setOwner(this);
		getBuildWrappersList().setOwner(this);

	}

	public CurrentBuildState getCurrentStateByNumber(int number) {
		return dynamicBuildRepository.getCurrentStateByNumber(this, number);
	}
}
