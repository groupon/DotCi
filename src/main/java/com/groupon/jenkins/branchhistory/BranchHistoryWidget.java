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

import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import com.groupon.jenkins.util.GReflectionUtils;
import hudson.widgets.BuildHistoryWidget;
import hudson.widgets.HistoryWidget;
import org.kohsuke.stapler.Header;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public class BranchHistoryWidget<T extends DbBackedBuild> extends BuildHistoryWidget<T> {

    protected static final int BUILD_COUNT = 30;
    protected static final String MY_BUILDS_BRANCH = "mine";
    private final String branch;
    private final BranchHistoryWidgetModel<T> model;

    public BranchHistoryWidget(final DynamicProject owner, final Adapter<? super T> adapter, final DynamicBuildRepository dynamicBuildRepository, final String branch) {
        super(owner, new MongoRunList<>(new BranchHistoryWidgetModel<>(owner, dynamicBuildRepository, branch)), adapter);
        this.model = new BranchHistoryWidgetModel<>(owner, dynamicBuildRepository, branch);
        this.branch = branch;
    }

    @Override
    public void doAjax(final StaplerRequest req, final StaplerResponse rsp, @Header("n") final String n) throws IOException, ServletException {

        if (n == null) {
            throw HttpResponses.error(SC_BAD_REQUEST, new IllegalArgumentException("Missing the 'n' HTTP header"));
        }

        rsp.setContentType("text/html;charset=UTF-8");

        final List<T> items = new LinkedList<>();

        String nn = null;

        // TODO refactor getBuildsAfter and database query to be getBuildsAfterAndEqual
        final Iterable<T> builds = this.model.getBuildsAfter(Integer.parseInt(n) - 1);
        for (final T t : builds) {
            if (this.adapter.compare(t, n) >= 0) {
                items.add(t);
                if (this.adapter.isBuilding(t)) {
                    nn = this.adapter.getKey(t);
                }
            } else {
                break;
            }
        }

        if (nn == null) {
            if (items.isEmpty()) {
                nn = n;
            } else {
                nn = this.adapter.getNextKey(this.adapter.getKey(items.get(0)));
            }
        }

        this.baseList = items;
        GReflectionUtils.setField(HistoryWidget.class, "firstTransientBuildKey", this, nn);

        rsp.setHeader("n", nn);

        req.getView(this, "ajaxBuildHistory.jelly").forward(req, rsp);
    }

    @Override
    public String getNextBuildNumberToFetch() {
        return this.model.getNextBuildToFetch(this.baseList, this.adapter).getBuildNumber(this.adapter);
    }

    public Object getBranch() {
        return this.branch;
    }

}
