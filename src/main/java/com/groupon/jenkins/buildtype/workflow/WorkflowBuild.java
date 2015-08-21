package com.groupon.jenkins.buildtype.workflow;

import com.groupon.jenkins.buildtype.util.shell.*;
import com.groupon.jenkins.dynamic.build.*;
import com.groupon.jenkins.dynamic.build.execution.*;
import com.groupon.jenkins.dynamic.buildtype.*;
import hudson.*;
import hudson.model.*;
import hudson.model.Queue;
import jenkins.model.*;
import org.acegisecurity.*;
import org.jenkinsci.plugins.workflow.cps.*;
import org.jenkinsci.plugins.workflow.flow.*;
import org.jenkinsci.plugins.workflow.graph.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

@Extension
public class WorkflowBuild extends BuildType {

    @Override
    public String getDescription() {
        return "Workflow Build";
    }

    @Override
    public Result runBuild(DynamicBuild build, BuildExecutionContext buildExecutionContext, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        ShellScriptRunner shellScriptRunner = new ShellScriptRunner(buildExecutionContext, listener);
        FlowDefinition definition = getFlowDefinition(shellScriptRunner);
        if (definition == null) {
            throw new AbortException("No flow definition, cannot run");
        }
        FlowExecution newExecution = null;
        try {
            newExecution = definition.create(getOwner(shellScriptRunner), listener, new ArrayList<Action>());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        FlowExecutionList.get().register(owner);
//        newExecution.addListener(new GraphL());
//        completed = new AtomicBoolean();
//        logsToCopy = new LinkedHashMap<String,Long>();
//        execution = newExecution;
        newExecution.start();
        newExecution.isComplete();
        return Result.SUCCESS;
    }

    private FlowDefinition getFlowDefinition(final ShellScriptRunner shellScriptRunner) {
        return new CpsFlowDefinition("echo 'purr'",true);

    }


    public FlowExecutionOwner getOwner(final ShellScriptRunner shellScriptRunner) {
        return new FlowExecutionOwner() {
            @Nonnull
            @Override
            public FlowExecution get() throws IOException {
                return new FlowExecution() {
                    @Override
                    public void start() throws IOException {

                        ShellCommands commands = new ShellCommands(Arrays.asList("echo meow"));
                        try {
                            shellScriptRunner.runScript(commands);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public FlowExecutionOwner getOwner() {
                        return null;
                    }

                    @Override
                    public List<FlowNode> getCurrentHeads() {
                        return null;
                    }

                    @Override
                    public boolean isCurrentHead(FlowNode flowNode) {
                        return false;
                    }

                    @Override
                    public void interrupt(Result result, CauseOfInterruption... causeOfInterruptions) throws IOException, InterruptedException {

                    }

                    @Override
                    public void addListener(GraphListener graphListener) {

                    }

                    @Override
                    public FlowNode getNode(String s) throws IOException {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public Authentication getAuthentication() {
                        return null;
                    }

                    @Override
                    public List<Action> loadActions(FlowNode flowNode) throws IOException {
                        return null;
                    }

                    @Override
                    public void saveActions(FlowNode flowNode, List<Action> list) throws IOException {

                    }
                };
            }

            @Override
            public File getRootDir() throws IOException {
                return null;
            }

            @Override
            public Queue.Executable getExecutable() throws IOException {
                return null;
            }

            @Override
            public String getUrl() throws IOException {
                return null;
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }
        };
    }
}
