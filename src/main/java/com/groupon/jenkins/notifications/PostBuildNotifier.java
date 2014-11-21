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
package com.groupon.jenkins.notifications;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.extensions.DotCiExtension;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;

public abstract class PostBuildNotifier extends DotCiExtension {


    public enum Type {
        FAILURE_AND_RECOVERY, ALL
    }

    private final String name;
    private Object options;

    public PostBuildNotifier(String name) {
        this.name = name;
    }

    protected boolean didBranchRecover(DynamicBuild build, BuildListener listener) {
        Run r = build.getPreviousFinishedBuildOfSameBranch(listener);
        return r != null && r.getResult().equals(Result.FAILURE);
    }

    public boolean perform(DynamicBuild build, BuildListener listener) {
        return doesBuildNeedNotification(build, listener) ? notify(build, listener) : true;
    }

    protected abstract Type getType();

    protected abstract boolean notify(DynamicBuild build, BuildListener listener);

    protected boolean doesBuildNeedNotification(DynamicBuild build, BuildListener listener) {
        if (getType().equals(Type.ALL)) {
            return true;
        }
        if (Result.SUCCESS.equals(build.getResult()))
            return didBranchRecover(build, listener);
        return Result.FAILURE.equals(build.getResult());
    }

    protected String getNotificationMessage(DynamicBuild build, BuildListener listener) {
        String subject;
        if (Result.FAILURE.equals(build.getResult())) {
            subject = "Build  failed for  branch " + build.getCurrentBranch() + "  " + build.getParent().getName() + " - " + build.getNumber();
        } else {
            subject = "Build recovered for branch " + build.getCurrentBranch() + "  " + build.getParent().getName() + " - " + build.getNumber();
        }
        return subject;
    }



    public void setOptions(Object options) {
        this.options = options;
    }

    public String getName() {
        return name;
    }

    public Object getOptions() {
        return options;
    }
}
