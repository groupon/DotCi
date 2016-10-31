package com.groupon.jenkins.dynamic.build;

import hudson.model.Executor;
import jenkins.model.Jenkins;

public class NullExecutor extends Executor {

    private final long startTimeInMillis;

    public NullExecutor(final long startTimeInMillis) {
        super(Jenkins.getInstance().createComputer(), 0);
        this.startTimeInMillis = startTimeInMillis;
    }

    @Override
    public long getElapsedTime() {
        return System.currentTimeMillis() - this.startTimeInMillis;
    }
}
