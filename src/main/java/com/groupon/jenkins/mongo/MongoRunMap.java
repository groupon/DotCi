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
package com.groupon.jenkins.mongo;

import com.google.common.collect.Lists;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class MongoRunMap<P extends DbBackedProject<P, B>, B extends DbBackedBuild<P, B>> implements SortedMap<Integer, B> {

    private final DbBackedProject<P, B> project;
    private final DynamicBuildRepository dynamicBuildRepository;
    private Collection<B> values;

    public MongoRunMap(final DbBackedProject<P, B> project) {
        this.project = project;
        this.dynamicBuildRepository = SetupConfig.get().getDynamicBuildRepository();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(final Object key) {
        if (!(key instanceof Integer)) {
            return this.dynamicBuildRepository.hasBuild(this.project, (Integer) key);
        }
        return false;
    }

    @Override
    public boolean containsValue(final Object value) {
        if (!(value instanceof DbBackedBuild)) {
            return this.dynamicBuildRepository.hasBuild((DbBackedBuild) value);
        }
        return false;
    }

    @Override
    public B get(final Object key) {
        return this.dynamicBuildRepository.<B>getBuild(this.project, (Integer) key);
    }

    @Override
    public boolean isEmpty() {
        return this.dynamicBuildRepository.hasBuilds(this.project);
    }

    @Override
    public B put(final Integer key, final B value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<? extends Integer, ? extends B> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public B remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return this.dynamicBuildRepository.getBuildCount(this.project);
    }

    @Override
    public Comparator<? super Integer> comparator() {
        return new Comparator<Integer>() {
            @Override
            public int compare(final Integer o1, final Integer o2) {
                return o1.compareTo(o2);
            }
        };
    }

    @Override
    public Set<Entry<Integer, B>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer firstKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<Integer, B> headMap(final Integer toKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Integer> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer lastKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<Integer, B> subMap(final Integer fromKey, final Integer toKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<Integer, B> tailMap(final Integer fromKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<B> values() {
        if (this.values == null) {
            this.values = Lists.newArrayList(SetupConfig.get().getDynamicBuildRepository().<B>latestBuilds(this.project, 20));
        }
        return this.values;
    }

}
