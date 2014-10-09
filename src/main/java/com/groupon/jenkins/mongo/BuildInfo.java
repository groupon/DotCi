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

import hudson.Util;
import hudson.model.Run;

import java.util.Calendar;

public class BuildInfo {
    private final Calendar timestamp;
    private final String url;
    private final String name;
    private final String result;

    public BuildInfo(Run run) {
        this.timestamp = run.getTimestamp();
        this.url = "/" + run.getUrl();
        this.name = run.getFullDisplayName();
        this.result = run.isBuilding() ? "Building" : run.getResult().toString();
    }

    public String getDisplayTime() {
        return Util.getPastTimeString(System.currentTimeMillis() - getTimestamp().getTimeInMillis()) + " ago";
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getResult() {
        return result;
    }

}
