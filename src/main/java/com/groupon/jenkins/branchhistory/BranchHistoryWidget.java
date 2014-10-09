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

import hudson.util.RunList;
import hudson.widgets.BuildHistoryWidget;
import hudson.widgets.HistoryWidget;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;

import org.kohsuke.stapler.Header;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.util.GReflectionUtils;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public class BranchHistoryWidget<T extends DbBackedBuild> extends BuildHistoryWidget<T> {

    protected static final int BUILD_COUNT = 30;
    protected static final String MY_BUILDS_BRANCH = "mine";
    private final String branch;
    private final BranchHistoryWidgetModel<T> model;

    public BranchHistoryWidget(DynamicProject owner, RunList<T> runList, hudson.widgets.HistoryWidget.Adapter<? super T> adapter, DynamicBuildRepository dynamicBuildRepository, String branch) {
        super(owner, runList, adapter);
        this.model = new BranchHistoryWidgetModel<T>(owner, dynamicBuildRepository, branch);
        this.branch = branch;
        this.baseList = this.model.getBaseList();
    }

    @Override
    public void doAjax(StaplerRequest req, StaplerResponse rsp, @Header("n") String n) throws IOException, ServletException {

        if (n == null) {
            throw HttpResponses.error(SC_BAD_REQUEST, new IllegalArgumentException("Missing the 'n' HTTP header"));
        }

        rsp.setContentType("text/html;charset=UTF-8");

        List<T> items = new LinkedList<T>();

        String nn = null;

        Iterable<T> builds = model.getBuildsAfter(n);
        for (T t : builds) {
            if (adapter.compare(t, n) >= 0) {
                items.add(t);
                if (adapter.isBuilding(t)) {
                    nn = adapter.getKey(t);
                }
            } else {
                break;
            }
        }

        if (nn == null) {
            if (items.isEmpty()) {
                nn = n;
            } else {
                nn = adapter.getNextKey(adapter.getKey(items.get(0)));
            }
        }

        baseList = items;
        GReflectionUtils.setField(HistoryWidget.class, "firstTransientBuildKey", this, nn);

        rsp.setHeader("n", nn);

        req.getView(this, "ajaxBuildHistory.jelly").forward(req, rsp);
    }

    @Override
    public String getNextBuildNumberToFetch() {
        return model.getNextBuildToFetch(baseList, adapter).getBuildNumber(adapter);
    }

    public Object getBranch() {
        return branch;
    }

}
