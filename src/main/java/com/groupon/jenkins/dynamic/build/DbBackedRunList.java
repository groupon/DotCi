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

import com.groupon.jenkins.SetupConfig;
import hudson.util.RunList;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Iterators;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;

public class DbBackedRunList<P extends DbBackedProject<P, B>, B extends DbBackedBuild<P, B>, R extends DbBackedBuild<P, B>> extends RunList<R> {

    private final DbBackedProject<P, B> project;
    private final DynamicBuildRepository dynamicBuildRepository;

    public DbBackedRunList(DbBackedProject<P, B> project) {
        this.project = project;
        this.dynamicBuildRepository = SetupConfig.get().getDynamicBuildRepository();
    }

    @Override
    public int size() {
        return dynamicBuildRepository.getBuildCount(project);
    }

    @Override
    public R get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<R> subList(int fromIndex, int toIndex) {
        List<R> r = new LinkedList<R>();
        Iterator<R> itr = iterator();
        Iterators.skip(itr, fromIndex);
        for (int i = toIndex - fromIndex; i > 0; i--) {
            if (itr.hasNext()) {
                r.add(itr.next());
            }
        }
        return r;
    }

    @Override
    public int indexOf(Object o) {
        int index = 0;
        for (R r : this) {
            if (r.equals(o)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int a = -1;
        int index = 0;
        for (R r : this) {
            if (r.equals(o)) {
                a = index;
            }
            index++;
        }
        return a;
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public R getFirstBuild() {
        return dynamicBuildRepository.<R> getFirstBuild(project);
    }

    @Override
    public R getLastBuild() {
        return dynamicBuildRepository.<R> getFirstBuild(project);
    }

    @Override
    public Iterator<R> iterator() {
        return dynamicBuildRepository.<R> latestBuilds(project, 20).iterator();
    }

}
