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

import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.util.GReflectionUtils;
import hudson.widgets.BuildHistoryWidget;
import hudson.widgets.HistoryWidget;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.kohsuke.stapler.*;

import javax.servlet.ServletException;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public class BranchHistoryWidget<T extends DbBackedBuild> extends BuildHistoryWidget<T> {

    protected static final int BUILD_COUNT = 30;
    protected static final String MY_BUILDS_BRANCH = "mine";
    private final String branch;
    private final BranchHistoryWidgetModel<T> model;

    public BranchHistoryWidget(DynamicProject owner,  hudson.widgets.HistoryWidget.Adapter<? super T> adapter, DynamicBuildRepository dynamicBuildRepository, String branch) {
        super(owner, new MongoRunList<T>(new BranchHistoryWidgetModel<T>(owner, dynamicBuildRepository, branch)), adapter);
        this.model = new BranchHistoryWidgetModel<T>(owner, dynamicBuildRepository, branch);
        this.branch = branch;
    }

    public void doAjax(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        req.getView(this, "ajax_build_history.jelly").forward(req, rsp);
    }

    public Iterable<T> getAjaxList() {
        StaplerRequest req = Stapler.getCurrentRequest();
        int firstBuildNumber = Integer.parseInt(req.getParameter("firstBuildNumber"));
        int lastBuildNumber = Integer.parseInt(req.getParameter("lastBuildNumber"));
        return model.getBuildsInProgress(firstBuildNumber, lastBuildNumber);
    }

    @Override
    public Iterable<T> getRenderList() {
        return this.baseList;
    }

    @Override
    public String getNextBuildNumberToFetch() {
        return model.getNextBuildToFetch(this.baseList, adapter).getBuildNumber(adapter);
    }

    public Object getBranch() {
        return branch;
    }

    public RowStyling getRowStyling(DynamicBuild dynamicBuild){
        return RowStyling.get(dynamicBuild);
    }


}
