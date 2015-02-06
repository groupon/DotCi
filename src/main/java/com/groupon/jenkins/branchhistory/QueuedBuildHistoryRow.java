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

import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import hudson.model.Queue;

import java.io.IOException;

public class QueuedBuildHistoryRow extends BuildHistoryRow {
    private Queue.Item item;
    private int number;

    public QueuedBuildHistoryRow(Queue.Item item, int number) {
        this.item = item;
        this.number = number;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getResult() {
        return "QUEUED";
    }

    @Override
    public String getIcon() {
        return "fa-paper-plane-o";
    }

    @Override
    public BuildCause.CommitInfo getCommit() {
        return new BuildCause.CommitInfo("Queued: " + item.getWhy(),item.getInQueueForString());
    }


    @Override
    public String getDisplayTime() {
        return "-";
    }

    @Override
    public String getDuration() {
        return "-"  ;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    @Override
    public String getCancelUrl() {
        return "/queue/cancelItem?id="+item.id;
    }
}
