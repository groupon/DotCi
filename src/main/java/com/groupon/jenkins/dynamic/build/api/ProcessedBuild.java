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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import hudson.matrix.Combination;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import org.kohsuke.stapler.export.Exported;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessedBuild extends Build {
    private final DynamicBuild build;

    public ProcessedBuild(final DynamicBuild build) {
        this.build = build;
    }

    @Override
    public int getNumber() {
        return this.build.getNumber();
    }

    @Override
    public String getResult() {
        return getResult(this.build);
    }

    private String getResult(final Run run) {
        if (isBuildInProgress(run)) {
            return "IN_PROGRESS";
        }
        return run.getResult().toString();
    }

    @Override
    public BuildCause.CommitInfo getCommit() {
        final BuildCause.CommitInfo commitInfo = this.build.getCause().getCommitInfo();
        if (commitInfo == null) {
            return BuildCause.CommitInfo.NULL_INFO;
        }
        return commitInfo;
    }


    @Override
    public String getDisplayTime() {
        return this.build.getDisplayTime();
    }

    @Override
    public long getDuration() {
        return getBuildDuration(this.build);
    }

    private long getBuildDuration(final Run build) {
        return build.isBuilding() ? System.currentTimeMillis() - build.getStartTimeInMillis() : build.getDuration();
    }

    @Override
    public String getDurationString() {
        return this.build.getDurationString();
    }

    @Override
    public boolean isCancelable() {
        return this.build.isBuilding();
    }

    @Override
    public String getCancelUrl() {
        return this.build.getUrl() + "/stop";
    }

    @Override
    public String getUrl() {
        return this.build.getUrl();
    }

    @Override
    public String getFullUrl() {
        return this.build.getFullUrl();
    }

    @Override
    public BuildCause getCause() {
        return this.build.getCause();
    }

    @Override
    public List<ParameterValue> getParameters() {
        if (this.build.getAction(ParametersAction.class) != null) {
            return this.build.getAction(ParametersAction.class).getParameters();
        }
        return Lists.newArrayList();
    }

    @Override
    public String getId() {
        return this.build.getProjectId() + this.build.getId();
    }


    @Exported
    public Iterable<Map> getAxisList() {

        final Iterable<Combination> layoutList = this.build.getLayouter().list();
        final Iterable<Map> subBuildInfo = Iterables.transform(layoutList, new Function<Combination, Map>() {
            @Override
            public Map apply(@Nullable final Combination combination) {
                final HashMap subBuild = new HashMap();
                subBuild.putAll(combination);
                final hudson.model.Build run = ProcessedBuild.this.build.getRun(combination);
                subBuild.putAll(getSubBuildInfo((DbBackedBuild) run));
                return subBuild;
            }
        });

        final ArrayList<Map> subBuilds = Iterables.size(layoutList) > 1 ? Lists.newArrayList(subBuildInfo) : new ArrayList<>();
        subBuilds.add(getMainBuildInfo(this.build));
        return subBuilds;
    }

    private Map getMainBuildInfo(final DynamicBuild build) {
        final HashMap<String, Object> buildInfo = new HashMap<>();
        buildInfo.put("script", "main");
        buildInfo.putAll(getSubBuildInfo(build));
        return buildInfo;
    }

    private Map getSubBuildInfo(final DbBackedBuild run) {
        final HashMap<String, Object> subBuild = new HashMap<>();
        subBuild.put("result", getResult(run));
        if (run != null) {
            subBuild.put("url", run.getUrl());
            subBuild.put("duration", getBuildDuration(run));
        } else {
            subBuild.put("url", this.build.getUrl());
        }
        return subBuild;
    }

    private boolean isBuildInProgress(final Run run) {
        return run == null || run.isBuilding();
    }
}
