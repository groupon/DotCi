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

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.api.BuildHistoryRow;
import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import hudson.model.Result;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility = 1)
public class ProcessedBuildHistoryRow extends BuildHistoryRow {
    private DynamicBuild build;

    public ProcessedBuildHistoryRow(DynamicBuild build){
        this.build = build;
    }

    @Override
    @Exported
    public int getNumber(){
        return build.getNumber();
    }
    @Override
    @Exported
    public String getResult(){
        if(build.isBuilding()){
            return "IN_PROGRESS";
        }
        return build.getResult().toString();
    }

    @Override
    @Exported
    public BuildCause.CommitInfo getCommit() {
        BuildCause.CommitInfo commitInfo = build.getCause().getCommitInfo();
        if(commitInfo == null){
           return BuildCause.CommitInfo.NULL_INFO;
        }
        return commitInfo;
    }


    @Override
    @Exported
    public String getDisplayTime(){
        return  build.getDisplayTime();
    }

    @Override
    @Exported
    public String getDuration() {
        return build.getDurationString();
    }

    @Override
    @Exported
    public boolean isCancelable() {
        return build.isBuilding();
    }

    @Override
    @Exported
    public String getCancelUrl() {
        return build.getUrl() + "/stop";
    }
}
