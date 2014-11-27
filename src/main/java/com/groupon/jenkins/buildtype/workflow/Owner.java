package com.groupon.jenkins.buildtype.workflow;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import hudson.model.Queue;
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;

import java.io.File;
import java.io.IOException;

public  final class Owner extends FlowExecutionOwner {
        private final String job;
        private final String id;
        private transient  DynamicBuild build;
      public  Owner(DynamicBuild run) {
            job = run.getParent().getFullName();
            id = run.getId();
            this.build = run;
        }
        private String key() {
            return job + '/' + id;
        }
        @Override public FlowExecution get() throws IOException {
            return new CpsFlowExecution("echo 'meow'",this);
        }
        @Override public File getRootDir() throws IOException {
            return build.getRootDir();
        }
        @Override public Queue.Executable getExecutable() throws IOException {
            return build;
        }
        @Override public String getUrl() throws IOException {
            return build.getUrl();
        }
        @Override public String toString() {
            return "Owner[" + key() + ":" + build + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Owner)) {
                return false;
            }
            Owner that = (Owner) o;
            return job.equals(that.job) && id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return job.hashCode() ^ id.hashCode();
        }
        private static final long serialVersionUID = 1;
    }