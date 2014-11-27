package com.groupon.jenkins.buildtype.workflow;

import com.google.common.util.concurrent.SettableFuture;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import com.groupon.jenkins.dynamic.buildtype.BuildType;
import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.model.StreamBuildListener;
import hudson.util.NullStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import jenkins.model.CauseOfInterruption;
import jenkins.model.InterruptedBuildAction;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionList;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;

/**
 * Created by surya on 11/26/2014.
 */
@Extension
public class WorkflowBuild extends BuildType {
    private Jenkins jenkins;

    private transient SettableFuture<FlowExecution> executionPromise = SettableFuture.create();
    private transient AtomicBoolean completed;
    private FlowExecution execution;

    @Override
    public String getDescription() {
        return "Workflow";
    }

    @Override
    public Result runBuild(DynamicBuild build, BuildExecutionContext buildExecutionContext, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        this.execution = new Owner(build).get();
        //((CpsFlowExecution)execution).programPromise = executionPromise;
        //return new CpsFlowExecution(sandbox ? script : ScriptApproval.get().using(script, GroovyLanguage.get()), sandbox, owner);
        executionPromise.set(execution);
        StreamBuildListener meowListener = new StreamBuildListener(listener.getLogger(), Charset.defaultCharset());
        execution.addListener(new GraphL(build,execution,meowListener));
        listener.started(build.getCauses());
        completed = new AtomicBoolean();
        execution.start();
        executionPromise.set(execution);
        waitForCompletion(meowListener,execution,build);
        return Result.SUCCESS;
    }
    void waitForCompletion(StreamBuildListener listener, FlowExecution execution, DynamicBuild build) {
        jenkins = Jenkins.getInstance();
        synchronized (completed) {
            while (!completed.get()) {
                if (jenkins == null || jenkins.isTerminating()) {
                    // Stop writing content, in case a new set of objects gets loaded after in-VM restart and starts writing to the same file:
                    listener.closeQuietly();
                    listener = new StreamBuildListener(new NullStream());
                    break;
                }
                try {
                    completed.wait(1000);
                } catch (InterruptedException x) {
                    try {
                        execution.interrupt(Result.ABORTED);
                    } catch (Exception x2) {
                    }
                    Executor exec = Executor.currentExecutor();
                    if (exec != null) {
                        exec.recordCauseOfInterruption(build, listener);
                    }
                }
//                copyLogs();
            }
        }
    }
    private void finish(Result r, DynamicBuild build, FlowExecution execution, StreamBuildListener listener) {
      //  LOGGER.log(Level.INFO, "{0} completed: {1}", new Object[] {this, r});
        build.setResult(r);
        // TODO set duration
        //RunListener.fireCompleted(WorkflowRun.this, listener);
        Throwable t = execution.getCauseOfFailure();
        if (t instanceof AbortException) {
            listener.error(t.getMessage());
        } else if (t instanceof FlowInterruptedException) {
            List<CauseOfInterruption> causes = ((FlowInterruptedException) t).getCauses();
            build.addAction(new InterruptedBuildAction(causes));
            for (CauseOfInterruption cause : causes) {
                cause.print(listener);
            }
        } else if (t != null) {
            t.printStackTrace(listener.getLogger());
        }
        listener.finished(build.getResult());
        listener.closeQuietly();
        try {
            build.save();
             build.getParent().logRotate();
        } catch (Exception x) {
    //        LOGGER.log(Level.WARNING, null, x);
        }
     //   build.onEndBuilding();
        assert completed != null;
        synchronized (completed) {
            completed.set(true);
            completed.notifyAll();
        }
        FlowExecutionList.get().unregister(execution.getOwner());
    }
    private final class GraphL implements GraphListener {
        private DynamicBuild build;
        private FlowExecution execution;
        private StreamBuildListener meowListener;

        public GraphL(DynamicBuild build, FlowExecution execution, StreamBuildListener meowListener) {

            this.build = build;
            this.execution = execution;
            this.meowListener = meowListener;
        }

        @Override public void onNewHead(FlowNode node) {
            synchronized (completed) {
           //     copyLogs();
            //    logsToCopy.put(node.getId(), 0L);
            }
            node.addAction(new TimingAction());

            //listener.getLogger().println("Running: " + node.getDisplayName());
            if (node instanceof FlowEndNode) {
                finish(((FlowEndNode) node).getResult(),build,execution,meowListener);
            } else {
            //    try {
             //       save();
            //    } catch (IOException x) {
              //      LOGGER.log(Level.WARNING, null, x);
             //   }
            }
        }
    }


}
