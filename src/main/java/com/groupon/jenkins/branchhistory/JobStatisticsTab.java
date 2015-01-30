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

package com.groupon.jenkins.branchhistory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import net.sf.json.JSONSerializer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class JobStatisticsTab extends HistoryTab {
    private DynamicProject project;

    public JobStatisticsTab(DynamicProject project) {
        this.project = project;
    }

    @Override
    public String getUrl() {
        return "job-statistics";
    }

    @Override
    public String getName() {
        return "Statistics";
    }

    @Override
    public boolean isRemovable() {
        return false;
    }
    public String getBuildTimes() throws IOException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        List<DbBackedBuild> builds = SetupConfig.get().getDynamicBuildRepository().getBuilds(project, "master",  cal,Calendar.getInstance());;
        //Map<String,Object> buildTimes = new HashMap<String,Object>();
        List<Object> buildNumbers = new ArrayList<Object> ();
        buildNumbers.add("x");
        List<Object> buildTimes = new ArrayList<Object>();
        buildTimes.add("Time");

        for(DbBackedBuild build : builds){
           buildNumbers.add(build.getNumber());
            buildTimes.add(TimeUnit.MILLISECONDS.toMinutes(build.getDuration()));
        }

        Map<String,Object> buildTimesMap = new HashMap<String,Object>();
        buildTimesMap.put("buildNumbers",buildNumbers.toArray());
        buildTimesMap.put("buildTimes", buildTimes.toArray());

        return JSONSerializer.toJSON(buildTimesMap).toString(0);
    }

}
