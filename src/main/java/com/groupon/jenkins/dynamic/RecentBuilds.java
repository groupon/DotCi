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

package com.groupon.jenkins.dynamic;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.api.ProcessedBuild;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

@ExportedBean
public class RecentBuilds {
    private List<RecentProject> recentProjects;

    public RecentBuilds(Iterable<DynamicBuild> builds) {
        this.recentProjects = groupByProject(builds);
    }

    private List<RecentProject> groupByProject(Iterable<DynamicBuild> builds) {
        final ImmutableListMultimap<String, DynamicBuild> groupedByProject = Multimaps.index(builds, new Function<DynamicBuild, String>() {
            @Override
            public String apply(DynamicBuild build) {
                return build.getParent().getFullName();
            }
        });
        Iterable<RecentProject> groupedRecentProjects = Iterables.transform(groupedByProject.keySet(), new Function<String, RecentProject>() {
            @Override
            public RecentProject apply(String projectName) {
                return new RecentProject(projectName, groupedByProject.get(projectName));
            }
        });
        return Lists.newArrayList(groupedRecentProjects);
    }

    @Exported(inline = true)
    public List<RecentProject> getRecentProjects() {
        return this.recentProjects;
    }


    @ExportedBean
    public static class RecentProject {
        private String name;
        private Iterable<ProcessedBuild> builds;

        public RecentProject(String projectName, ImmutableList<DynamicBuild> dynamicBuilds) {
            this.name = projectName;
            this.builds = Iterables.transform(dynamicBuilds, new Function<DynamicBuild, ProcessedBuild>() {
                @Override
                public ProcessedBuild apply(DynamicBuild build) {
                    return new ProcessedBuild(build);
                }
            });
        }

        @Exported(inline = true)
        public String getName() {
            return name;
        }

        @Exported(inline = true)
        public List<ProcessedBuild> getBuilds() {
            return Lists.newArrayList(builds);
        }


    }

}
