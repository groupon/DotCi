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
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

public class BuildHistory extends ApiModel {
    private static final int BUILD_COUNT = 20;
    private DynamicProject dynamicProject;

    public BuildHistory(DynamicProject dynamicProject) {
        this.dynamicProject = dynamicProject;
    }

    public void getDynamic(String branch, StaplerRequest req, StaplerResponse rsp) throws IOException {
        JsonResponse.render(rsp,getBuilds(branch));
    }

    public Iterable<BuildHistoryRow> getBuilds(String branch) {
        return toUiBuilds( filterSkipped(isMyBuilds(branch) ? getDynamicBuildRepository().<DynamicBuild>getCurrentUserBuilds(dynamicProject, BUILD_COUNT) : getDynamicBuildRepository().<DynamicBuild>getLast(dynamicProject, BUILD_COUNT, branch)));
    }

    private Iterable<BuildHistoryRow> toUiBuilds(Iterable<DynamicBuild> builds) {
        return Iterables.transform(builds, new Function<DynamicBuild, BuildHistoryRow>() {
            @Override
            public BuildHistoryRow apply(DynamicBuild input) {
                return new ProcessedBuildHistoryRow(input);
            }
        });
    }

    private DynamicBuildRepository getDynamicBuildRepository(){
        return SetupConfig.get().getDynamicBuildRepository();
    }

    private boolean isMyBuilds(String branch) {
        return "Mine".equalsIgnoreCase(branch);
    }

    private Iterable<DynamicBuild> filterSkipped(Iterable<DynamicBuild> builds) {
        return Iterables.filter(builds, new Predicate<DynamicBuild>() {
            @Override
            public boolean apply(DynamicBuild build) {
                return !build.isSkipped();
            }
        });
    }



}
