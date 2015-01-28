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
import com.groupon.jenkins.dynamic.build.DynamicProjectBranchTabsProperty;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;
import hudson.widgets.BuildHistoryWidget;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;


public class BranchHistoryWidget<T extends DbBackedBuild> extends BuildHistoryWidget<T> {

    protected static final int BUILD_COUNT = 30;
    protected static final String MY_BUILDS_BRANCH = "mine";
    private final BranchHistoryWidgetModel<T> model;


    protected static String getCurrentBranch(DynamicProject owner) {
        return (String) Stapler.getCurrentRequest().getSession().getAttribute("branchView" + owner.getName());
    }

    public BranchHistoryWidget(DynamicProject owner,  hudson.widgets.HistoryWidget.Adapter<? super T> adapter, DynamicBuildRepository dynamicBuildRepository) {
        super(owner, new MongoRunList<T>(new BranchHistoryWidgetModel<T>(owner, dynamicBuildRepository, getCurrentBranch(owner))), adapter);
        this.model = new BranchHistoryWidgetModel<T>(owner, dynamicBuildRepository, getCurrentBranch(owner));
    }

    public void doSwitchTab(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        String tab  = req.getRestOfPath().replace("/","");
        req.getSession().setAttribute("tabSelection" + owner.getName(), tab);
        rsp.forwardToPreviousPage(req);
    }

    public void doAddNewBranchTab(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        String tab  = req.getParameter("branch");
        if(StringUtils.isNotEmpty(tab)){
            DynamicProjectBranchTabsProperty branchTabsProperty = getTabsProperty();
            if(branchTabsProperty == null){
                branchTabsProperty =     new DynamicProjectBranchTabsProperty(tab);
                ((DynamicProject)owner).addProperty(branchTabsProperty);
            }else{
                branchTabsProperty.addBranch(tab);
            }
            ((DynamicProject)owner).save();
        }
        rsp.forwardToPreviousPage(req);
    }

    public void doRemoveTab(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        String tab  = req.getParameter("branch");
        if(StringUtils.isNotEmpty(tab)){
            DynamicProjectBranchTabsProperty branchTabsProperty = getTabsProperty();
            branchTabsProperty.removeBranch(tab);
            ((DynamicProject)owner).save();
        }
        req.getSession().removeAttribute("branchView" + owner.getName());
        rsp.forwardToPreviousPage(req);
    }

    private DynamicProjectBranchTabsProperty getTabsProperty() {
        return  ((DynamicProject)owner).getProperty(DynamicProjectBranchTabsProperty.class);
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



    public RowStyling getRowStyling(DynamicBuild dynamicBuild){
        return RowStyling.get(dynamicBuild);
    }

    public Iterable<BuildHistoryTab> getTabs(){
        DynamicProjectBranchTabsProperty tabsProperty = getTabsProperty();
        return  BuildHistoryTab.getTabs(tabsProperty == null ? Collections.<String>emptyList():tabsProperty.getBranches());
    }
}
