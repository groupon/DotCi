package com.groupon.jenkins.dynamic.build;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

public interface BuildStopListener extends ExtensionPoint {

    static ExtensionList<BuildStopListener> all() {
        return ExtensionList.lookup(BuildStopListener.class);
    }

    void onStop(DynamicBuild build);
}

