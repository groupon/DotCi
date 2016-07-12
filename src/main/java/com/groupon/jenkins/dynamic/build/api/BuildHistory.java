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
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.util.JsonResponse;
import hudson.model.Queue;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BuildHistory extends ApiModel {
    private DynamicProject dynamicProject;

    public BuildHistory(DynamicProject dynamicProject) {
        this.dynamicProject = dynamicProject;
    }

    public void getDynamic(String branch, StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        int count = Integer.parseInt(req.getParameter("count"));
        JsonResponse.render(req, rsp,new BuildHistoryRsp( getBuilds(branch, count)));
    }

    public Iterable<Build> getBuilds(String branch,int count) {
        if("All".equalsIgnoreCase(branch)){
            branch =null;
        }
        Iterable<DynamicBuild> builds = isMyBuilds(branch) ? getDynamicBuildRepository().<DynamicBuild>getCurrentUserBuilds(dynamicProject, count,null) : getDynamicBuildRepository().<DynamicBuild>getLast(dynamicProject, count, branch,null);
        return Iterables.concat(getQueuedBuilds(), toUiBuilds(filterSkipped(builds)));
    }
    private boolean isMyBuilds(String branch) {
        return "Mine".equalsIgnoreCase(branch);
    }

    private Iterable<QueuedBuild> getQueuedBuilds() {
        int nextBuildNumber = dynamicProject.getNextBuildNumber();
        List<QueuedBuild> queuedBuilds = new ArrayList<QueuedBuild>();
        for(Queue.Item item: getQueuedItems()) {
            queuedBuilds.add(new QueuedBuild(item, nextBuildNumber++));
        }
        Collections.reverse(queuedBuilds);
        return queuedBuilds;
    }

    private Iterable<Build> toUiBuilds(Iterable<DynamicBuild> builds) {
        return Iterables.transform(builds, new Function<DynamicBuild, Build>() {
            @Override
            public Build apply(DynamicBuild input) {
                return new ProcessedBuild(input);
            }
        });
    }

    private DynamicBuildRepository getDynamicBuildRepository(){
        return SetupConfig.get().getDynamicBuildRepository();
    }


    private Iterable<DynamicBuild> filterSkipped(Iterable<DynamicBuild> builds) {
        return Iterables.filter(builds, new Predicate<DynamicBuild>() {
            @Override
            public boolean apply(DynamicBuild build) {
                return !build.isSkipped() && !build.isPhantom();
            }
        });
    }
    public List<Queue.Item> getQueuedItems() {
        LinkedList<Queue.Item> list = new LinkedList<Queue.Item>();
        for (Queue.Item item : Jenkins.getInstance().getQueue().getApproximateItemsQuickly()) {
            if (item.task == dynamicProject) {
                list.addFirst(item);
            }
        }
        return list;
    }


}
