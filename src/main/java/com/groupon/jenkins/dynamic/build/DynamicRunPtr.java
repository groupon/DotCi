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

import hudson.Util;
import hudson.matrix.Axis;
import hudson.matrix.Combination;
import hudson.model.Build;
import hudson.model.Queue;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.Stapler;

import java.util.List;

public final class DynamicRunPtr {
    private final Combination combination;
    private transient final DynamicBuild dynamicBuild;

    DynamicRunPtr(final Combination c, final DynamicBuild dynamicBuild) {
        this.combination = c;
        this.dynamicBuild = dynamicBuild;
    }

    public String toName(final List<Axis> axisList) {
        return this.combination.toString(axisList);
    }

    public Build getRun() {
        return this.dynamicBuild.getRun(this.combination);
    }

    public String getNearestRunUrl() {
        final
        Build r = getRun();
        if (r == null) {
            return null;
        }
        if (this.dynamicBuild.getNumber() == r.getNumber()) {
            return getShortUrl() + "/console";
        }
        return Stapler.getCurrentRequest().getContextPath() + '/' + r.getUrl();
    }

    public String getShortUrl() {
        return Util.rawEncode(this.combination.toString());
    }

    public String getTooltip() {
        final Build r = getRun();
        if (r != null) {
            return r.getIconColor().getDescription();
        }
        final Queue.Item item = Jenkins.getInstance().getQueue().getItem(this.dynamicBuild.getParent().getItem(this.combination));
        if (item != null) {
            return item.getWhy();
        }
        return null; // fall back
    }
}
