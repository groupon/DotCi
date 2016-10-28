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

import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.matrix.Layouter;

public class DynamicBuildLayouter extends Layouter<DynamicRunPtr> {
    private AxisList axisList;
    private DynamicBuild dynamicBuild;

    public DynamicBuildLayouter(AxisList axisList, DynamicBuild dynamicBuild) {
        super(axisList == null ? new AxisList() : axisList);
        this.axisList = axisList;
        this.dynamicBuild = dynamicBuild;
    }

    @Override
    protected DynamicRunPtr getT(Combination c) {
        return new DynamicRunPtr(c, dynamicBuild);
    }


    public Iterable<Combination> list() {
        return axisList == null ? new AxisList().list() : axisList.list();
    }
}
