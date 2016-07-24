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

import com.google.common.collect.Lists;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import hudson.util.RunList;

import java.util.Iterator;
import java.util.List;

public class DbBackedRunList<P extends DbBackedProject<P, B>, B extends DbBackedBuild<P, B>, R extends DbBackedBuild<P, B>> extends RunList<R> {

    private final DbBackedProject<P, B> project;
    private final DynamicBuildRepository dynamicBuildRepository;

    public DbBackedRunList(DbBackedProject<P, B> project) {
        this.project = project;
        this.dynamicBuildRepository = SetupConfig.get().getDynamicBuildRepository();
    }

    @Override
    public Iterator<R> iterator() {
        return null;
    }



    @Override
    public List subList(int fromIndex, int toIndex) {
        return Lists.newArrayList( dynamicBuildRepository.getBuilds(project,fromIndex));
    }

    @Override
    public int size() {
        return dynamicBuildRepository.getBuildCount(project);
    }
}
