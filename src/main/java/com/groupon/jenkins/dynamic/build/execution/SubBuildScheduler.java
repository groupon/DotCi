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

import com.groupon.jenkins.dynamic.build.CurrentBuildState;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicSubBuild;
import com.groupon.jenkins.dynamic.build.DynamicSubProject;
import com.groupon.jenkins.dynamic.build.DynamicSubProject.ParentBuildAction;
import hudson.Util;
import hudson.console.ModelHyperlinkNote;
import hudson.matrix.Combination;
import hudson.matrix.Messages;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Executor;
import hudson.model.ParametersAction;
import hudson.model.Queue;
import hudson.model.Queue.Item;
import hudson.model.Result;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SubBuildScheduler {

    private final DynamicBuild dynamicBuild;
    private final SubBuildRunner subBuildRunner;
    private final SubBuildFinishListener subBuildFinishListener;

    public SubBuildScheduler(final DynamicBuild build, final SubBuildRunner subBuildRunner, final SubBuildFinishListener subBuildFinishListener) {
        this.dynamicBuild = build;
        this.subBuildRunner = subBuildRunner;
        this.subBuildFinishListener = subBuildFinishListener;
    }

    public Result runSubBuilds(final Iterable<Combination> subBuildCombinations, final BuildListener listener) throws InterruptedException, IOException {
        final Iterable<DynamicSubProject> subProjects = getRunSubProjects(subBuildCombinations);
        scheduleSubBuilds(subBuildCombinations, this.subBuildFinishListener, listener);
        Result r = Result.SUCCESS;
        for (final DynamicSubProject c : subProjects) {
            final CurrentBuildState runState = waitForCompletion(c, listener);
            final Result runResult = getResult(runState);
            r = r.combine(runResult);
            listener.getLogger().println("Run " + c.getName() + " finished with : " + runResult);
//            subBuildFinishListener.runFinished(c.getBuildByNumber(dynamicBuild.getNumber()) );
        }
        return r;
    }

    private Result getResult(final CurrentBuildState run) {
        return run != null ? run.getResult() : Result.ABORTED;
    }

    protected void scheduleSubBuilds(final Iterable<Combination> subBuildCombinations, final SubBuildFinishListener subBuildFinishListener, final TaskListener listener) {
        for (final Combination subBuildCombination : subBuildCombinations) {
            final DynamicSubProject c = this.dynamicBuild.getSubProject(subBuildCombination);
            listener.getLogger().println(Messages.MatrixBuild_Triggering(ModelHyperlinkNote.encodeTo(c)));
            final List<Action> childActions = new ArrayList<>();
            childActions.addAll(Util.filter(this.dynamicBuild.getActions(), ParametersAction.class));
            childActions.add(new SubBuildExecutionAction(this.subBuildRunner, subBuildFinishListener));
            childActions.add(new ParentBuildAction(this.dynamicBuild));
            c.scheduleBuild(childActions, this.dynamicBuild.getCause());
        }
    }

    public CurrentBuildState waitForCompletion(final DynamicSubProject c, final TaskListener listener) throws InterruptedException {

        // wait for the completion
        int appearsCancelledCount = 0;
        while (true) {
            Thread.sleep(1000);
            final CurrentBuildState b = c.getCurrentStateByNumber(this.dynamicBuild.getNumber());
            if (b != null) { // its building or is done
                if (b.isBuilding()) {
                    continue;
                } else {
                    final Result buildResult = b.getResult();
                    if (buildResult != null) {
                        return b;
                    }
                }
            } else { // not building or done, check queue
                final Queue.Item qi = c.getQueueItem();
                if (qi == null) {
                    appearsCancelledCount++;
                    listener.getLogger().println(c.getName() + " appears cancelled: " + appearsCancelledCount);
                } else {
                    appearsCancelledCount = 0;
                }

                if (appearsCancelledCount >= 5) {
                    listener.getLogger().println(Messages.MatrixBuild_AppearsCancelled(ModelHyperlinkNote.encodeTo(c)));
                    return new CurrentBuildState("COMPLETED", Result.ABORTED);
                }
            }

        }
    }

    public void cancelSubBuilds(final PrintStream logger) {
        final Queue q = getJenkins().getQueue();
        synchronized (q) {
            final int n = this.dynamicBuild.getNumber();
            for (final Item i : q.getItems()) {
                final ParentBuildAction parentBuildAction = i.getAction(ParentBuildAction.class);
                if (parentBuildAction != null && this.dynamicBuild.equals(parentBuildAction.getParent())) {
                    q.cancel(i);
                }
            }
            for (final DynamicSubProject c : this.dynamicBuild.getAllSubProjects()) {
                final DynamicSubBuild b = c.getBuildByNumber(n);
                if (b != null && b.isBuilding()) {
                    final Executor exe = b.getExecutor();
                    if (exe != null) {
                        logger.println(Messages.MatrixBuild_Interrupting(ModelHyperlinkNote.encodeTo(b)));
                        exe.interrupt();
                    }
                }
            }
        }
    }

    public Iterable<DynamicSubProject> getRunSubProjects(final Iterable<Combination> combinations) {
        return this.dynamicBuild.getSubProjects(combinations);
    }

    private Jenkins getJenkins() {

        return Jenkins.getInstance();
    }


    public interface SubBuildFinishListener {
        public void runFinished(DynamicSubBuild subBuild) throws IOException;
    }

}
