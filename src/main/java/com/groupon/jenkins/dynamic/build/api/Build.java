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

import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import hudson.model.Cause;
import hudson.model.ParameterValue;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;


@ExportedBean
public abstract class Build {

    @Exported
    public abstract int getNumber();

    @Exported
    public abstract String getResult();


    @Exported(inline = true)
    public abstract BuildCause.CommitInfo getCommit();


    @Exported
    public abstract String getDisplayTime();

    @Exported
    public abstract long getDuration();

    @Exported
    public abstract String getDurationString();

    @Exported
    public abstract boolean isCancelable();

    @Exported
    public abstract String getCancelUrl();

    @Exported
    public abstract String getUrl();


    @Exported
    public abstract String getFullUrl();

    @Exported
    public abstract Cause getCause();

    @Exported
    public abstract List<ParameterValue> getParameters();

    @Exported(inline = true)
    public abstract String getId();

}
