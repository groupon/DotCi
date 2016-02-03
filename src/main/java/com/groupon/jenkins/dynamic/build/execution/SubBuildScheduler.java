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
import hudson.model.Label;
import hudson.model.ParametersAction;
import hudson.model.Queue;
import hudson.model.Queue.Item;
import hudson.model.Result;
import hudson.model.TaskListener;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import jenkins.model.Jenkins;

public class SubBuildScheduler {

    public interface SubBuildFinishListener{
      public void runFinished(DynamicSubBuild subBuild) throws IOException;
    }

    private final DynamicBuild dynamicBuild;

    private SubBuildRunner subBuildRunner;
    private SubBuildFinishListener subBuildFinishListener;

    public SubBuildScheduler(DynamicBuild build, SubBuildRunner subBuildRunner, SubBuildFinishListener subBuildFinishListener) {
        this.dynamicBuild = build;
        this.subBuildRunner = subBuildRunner;
        this.subBuildFinishListener = subBuildFinishListener;
    }

    public Result runSubBuilds(Iterable<Combination> subBuildCombinations, BuildListener listener) throws InterruptedException, IOException {
        return runSubBuilds(subBuildCombinations, listener, null);
    }

    public Result runSubBuilds(Iterable<Combination> subBuildCombinations, BuildListener listener, String subBuildLabel) throws InterruptedException, IOException {
            Iterable<DynamicSubProject> subProjects = getRunSubProjects(subBuildCombinations);

        scheduleSubBuilds(subBuildCombinations, listener, subBuildLabel);
        Result r = Result.SUCCESS;

        for (DynamicSubProject c : subProjects) {
            CurrentBuildState runState = waitForCompletion(c, listener);
            Result runResult = getResult(runState);
            r = r.combine(runResult);
            listener.getLogger().println("Run " + c.getName() + " finished with : " + runResult);
            subBuildFinishListener.runFinished(c.getBuildByNumber(dynamicBuild.getNumber()) );
        }
        return r;
    }

    private Result getResult(CurrentBuildState run) {
        return run != null ? run.getResult() : Result.ABORTED;
    }

    protected void scheduleSubBuilds(Iterable<Combination> subBuildCombinations, TaskListener listener, String subBuildLabel) {
        Label label = null;
        if (subBuildLabel != null) {
            label = Label.get(subBuildLabel);
        }

        for ( Combination subBuildCombination : subBuildCombinations) {
            DynamicSubProject c = dynamicBuild.getSubProject(subBuildCombination);
            listener.getLogger().println(Messages.MatrixBuild_Triggering(ModelHyperlinkNote.encodeTo(c)));
            List<Action> childActions = new ArrayList<Action>();
            childActions.addAll(Util.filter(dynamicBuild.getActions(), ParametersAction.class));

            childActions.add(new SubBuildExecutionAction(subBuildRunner));
            childActions.add(new ParentBuildAction(dynamicBuild));

            if (label != null) {
                childActions.add(new NodeAssignmentAction(label));
            }

            c.scheduleBuild(childActions, dynamicBuild.getCause());
        }
    }

    public CurrentBuildState waitForCompletion(DynamicSubProject c, TaskListener listener) throws InterruptedException {

        // wait for the completion
        int appearsCancelledCount = 0;
        while (true) {
            Thread.sleep(1000);
            CurrentBuildState b = c.getCurrentStateByNumber(dynamicBuild.getNumber());
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
                    return new CurrentBuildState("COMPLETED", Result.ABORTED);
                }
            }

        }
    }

    public void cancelSubBuilds(PrintStream logger) {
        Queue q = getJenkins().getQueue();
        synchronized (q) {
            final int n = dynamicBuild.getNumber();
            for (Item i : q.getItems()) {
                ParentBuildAction parentBuildAction = i.getAction(ParentBuildAction.class);
                if (parentBuildAction != null && dynamicBuild.equals(parentBuildAction.getParent())) {
                    q.cancel(i);
                }
            }
            for (DynamicSubProject c : dynamicBuild.getAllSubProjects()) {
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

    public Iterable<DynamicSubProject> getRunSubProjects(Iterable<Combination> combinations) {
        return dynamicBuild.getSubProjects(combinations);
    }


    private Jenkins getJenkins() {

        return Jenkins.getInstance();
    }

}
