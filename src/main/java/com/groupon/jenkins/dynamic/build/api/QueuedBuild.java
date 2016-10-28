/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.groupon.jenkins.dynamic.build.api;

import com.google.common.collect.Lists;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import hudson.model.Cause;
import hudson.model.ParameterValue;
import hudson.model.Queue;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class QueuedBuild extends Build {
    private final Queue.Item item;
    private final int number;

    public QueuedBuild(final Queue.Item item, final int number) {
        this.item = item;
        this.number = number;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getResult() {
        return "QUEUED";
    }

    @Override
    public BuildCause.CommitInfo getCommit() {
        final String[] buildParams = StringUtils.split(this.item.getParams(), "\n");
        String branch = "";
        for (final String buildParam : buildParams) {
            if (buildParam.startsWith("BRANCH=")) {
                branch = StringUtils.split(buildParam, "=")[1];
            }
        }
        return new BuildCause.CommitInfo("Queued: " + this.item.getWhy(), this.item.getInQueueForString(), branch);
    }


    @Override
    public String getDisplayTime() {
        return "-";
    }

    @Override
    public long getDuration() {
        return -1;
    }

    @Override
    public String getDurationString() {
        return "-";
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    @Override
    public String getCancelUrl() {
        return "/queue/cancelItem?id=" + this.item.getId();
    }

    @Override
    public String getUrl() {
        return this.item.getUrl();
    }

    @Override
    public String getFullUrl() {
        return this.item.getUrl();
    }

    @Override
    public BuildCause getCause() {
        for (final Cause cause : this.item.getCauses()) {
            if (cause instanceof BuildCause) {
                return (BuildCause) cause;
            }
        }
        return BuildCause.NULL_BUILD_CAUSE;
    }

    @Override
    public List<ParameterValue> getParameters() {
        return Lists.newArrayList();
    }

    @Override
    public String getId() {
        return this.item.getId() + "";
    }

}
