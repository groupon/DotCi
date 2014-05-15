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
package com.groupon.jenkins.dynamic.build.execution;

import hudson.Util;
import hudson.console.ModelHyperlinkNote;
import hudson.matrix.MatrixChildAction;
import hudson.matrix.Messages;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.Executor;
import hudson.model.Queue;
import hudson.model.Queue.Item;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import jenkins.model.Jenkins;

import com.groupon.jenkins.dynamic.build.CurrentBuildState;
import com.groupon.jenkins.dynamic.build.DynamicSubBuild;
import com.groupon.jenkins.dynamic.build.DynamicSubProject;
import com.groupon.jenkins.dynamic.build.DynamicSubProject.ParentBuildAction;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.buildconfiguration.plugins.DotCiPluginAdapter;

public class SubBuildScheduler {

	private final DynamicBuild build;
	private final SubBuildParamsAction subBuildParamsAction;

	public SubBuildScheduler(DynamicBuild build, SubBuildParamsAction subBuildParamsAction) {
		this.build = build;
		this.subBuildParamsAction = subBuildParamsAction;
	}

	public Result runSubBuilds(Iterable<DynamicSubProject> subProjects, BuildListener listener) throws InterruptedException, IOException {
		scheduleSubBuilds(subProjects, listener);
		Result r = Result.SUCCESS;
		for (DynamicSubProject c : subProjects) {
			CurrentBuildState runState = waitForCompletion(c, listener);
			Result runResult = getResult(runState);
			r = r.combine(runResult);
			listener.getLogger().println("Run " + c.getName() + " finished with : " + runResult);
			for (DotCiPluginAdapter plugin : build.getBuildConfiguration().getPlugins()) {
				plugin.runFinished(c.getBuildByNumber(build.getNumber()), build, listener);
			}
		}
		return r;
	}

	private Result getResult(CurrentBuildState run) {
		return run != null ? run.getResult() : Result.ABORTED;
	}

	protected void scheduleSubBuilds(Iterable<DynamicSubProject> subProjects, TaskListener listener) {
		for (DynamicSubProject c : subProjects) {
			listener.getLogger().println(Messages.MatrixBuild_Triggering(ModelHyperlinkNote.encodeTo(c)));
			List<Action> childActions = new ArrayList<Action>();
			childActions.addAll(Util.filter(build.getActions(), MatrixChildAction.class));
			childActions.add(subBuildParamsAction);
			c.scheduleBuild(childActions, build.getCause());
		}
	}

	public CurrentBuildState waitForCompletion(DynamicSubProject c, TaskListener listener) throws InterruptedException {

		// wait for the completion
		int appearsCancelledCount = 0;
		while (true) {
			Thread.sleep(1000);
			CurrentBuildState b = c.getCurrentStateByNumber(build.getNumber());
			if (b != null) { // its building or is done
				if (b.isBuilding()) {
					continue;
				} else {
					Result buildResult = b.getResult();
					if (buildResult != null) {
						return b;
					}
				}
			} else { // not building or done, check queue
				Queue.Item qi = c.getQueueItem();
				if (qi == null) {
					appearsCancelledCount++;
					listener.getLogger().println(c.getName() + " appears cancelled: " + appearsCancelledCount);
				} else {
					appearsCancelledCount = 0;
				}

				if (appearsCancelledCount >= 5) {
					listener.getLogger().println(Messages.MatrixBuild_AppearsCancelled(ModelHyperlinkNote.encodeTo(c)));
					return new CurrentBuildState("COMPLETED", Result.ABORTED.toString());
				}
			}

		}
	}

	protected void cancelSubBuilds(PrintStream logger) {
		Queue q = getJenkins().getQueue();
		synchronized (q) {
			final int n = build.getNumber();
			for (Item i : q.getItems()) {
				ParentBuildAction parentBuildAction = i.getAction(ParentBuildAction.class);
				if (parentBuildAction != null && build.equals(parentBuildAction.parent)) {
					q.cancel(i);
				}
			}
			for (DynamicSubProject c : build.getAllSubProjects()) {
				DynamicSubBuild b = c.getBuildByNumber(n);
				if (b != null && b.isBuilding()) {
					Executor exe = b.getExecutor();
					if (exe != null) {
						logger.println(Messages.MatrixBuild_Interrupting(ModelHyperlinkNote.encodeTo(b)));
						exe.interrupt();
					}
				}
			}
		}
	}

	private Jenkins getJenkins() {
		return Jenkins.getInstance();
	}

}
