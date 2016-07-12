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
package com.groupon.jenkins.branchhistory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import hudson.widgets.HistoryWidget;

import java.util.ArrayList;
import java.util.ListIterator;

class BranchHistoryWidgetModel<T extends DbBackedBuild> {

    private final String branch;
    private final DynamicBuildRepository dynamicBuildRepository;
    private final DynamicProject owner;

    public BranchHistoryWidgetModel(DynamicProject owner, DynamicBuildRepository dynamicBuildRepository, String branch) {
        this.owner = owner;
        this.dynamicBuildRepository = dynamicBuildRepository;
        this.branch = branch;
    }

    public Iterable<T> getBuildsAfter(int n) {
        return filterSkipped(isMyBuilds() ? dynamicBuildRepository.<T> getCurrentUserBuildsGreaterThan((DbBackedProject) owner, n) : dynamicBuildRepository.<T> getBuildGreaterThan((DbBackedProject) owner, n, branch));
    }

    public Iterable<T> getBaseList() {
        return filterSkipped(isMyBuilds() ? dynamicBuildRepository.<T> getCurrentUserBuilds(((DbBackedProject) owner), BranchHistoryWidget.BUILD_COUNT,null) : dynamicBuildRepository.<T> getLast((DbBackedProject) owner, BranchHistoryWidget.BUILD_COUNT, branch, null));
    }

    public OffsetBuild<T> getNextBuildToFetch(Iterable<T> builds, HistoryWidget.Adapter<? super T> adapter) {
        ArrayList<T> list = Lists.newArrayList(builds);
        if (!list.isEmpty()) {
            ListIterator<T> listIterator = list.listIterator(list.size());

            while (listIterator.hasPrevious()) {
                T record = listIterator.previous();
                if (adapter.isBuilding(record))
                    return new OffsetBuild<T>(record, 0);
            }

            return new OffsetBuild(list.get(0), 1);
        }
        return new OffsetBuild<T>(null, 0);
    }

    private Iterable<T> filterSkipped(Iterable<T> builds) {
        return Iterables.filter(builds, new Predicate<T>() {
            @Override
            public boolean apply(T build) {
                return !build.isSkipped() && !build.isPhantom();
            }
        });
    }

    private boolean isMyBuilds() {
        return BranchHistoryWidget.MY_BUILDS_BRANCH.equals(this.branch);
    }

    protected static class OffsetBuild<T> {
        private final T t;
        private final int offset;

        public OffsetBuild(T t, int offset) {
            this.t = t;
            this.offset = offset;
        }

        public String getBuildNumber(HistoryWidget.Adapter<? super T> adapter) {
            if (t == null) {
                return "1";
            }
            return String.valueOf(Integer.valueOf(adapter.getKey(t)) + offset);
        }
    }
}
