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

package com.groupon.jenkins.dynamic.build;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DynamicProjectBranchTabsProperty extends JobProperty<Job<?, ?>> {
    //For backward compatability
    private transient ArrayList<String> branches;

    private String branchTabs;

    public DynamicProjectBranchTabsProperty(String branchTabs) {
        this.branchTabs = branchTabs;
    }

    private ArrayList<String> parseBranches() {
        ArrayList<String> parsedBranches = new ArrayList<String>();
        if (!StringUtils.isEmpty(branchTabs)) {
            parsedBranches.addAll(trim());
        }
        return parsedBranches;
    }

    private List<String> trim() {
        return Lists.newArrayList(Iterables.transform(Arrays.asList(branchTabs.split("\\n")), new Function<String, String>() {
            @Override
            public String apply(@Nullable String input) {
                return input.trim();
            }
        }));
    }


    public List<String> getBranches() {
        return parseBranches();
    }

    public String getBranchTabs() {
        return branchTabs;
    }

    public void addBranch(String branch) {
        ArrayList<String> branches = parseBranches();
        branches.add(branch);
        save(branches);
    }


    public void removeBranch(String branch) {
        ArrayList<String> branches = parseBranches();
        branches.remove(branch);
        save(branches);
    }

    private void save(ArrayList<String> branches) {
        this.branchTabs = Joiner.on("\n").join(branches);
    }

    @Extension
    public static final class BranchTabsPropertyDescriptor extends JobPropertyDescriptor {

        @Override
        public String getDisplayName() {
            return "Branch Tabs";
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return DynamicProject.class.equals(jobType);
        }

        @Override
        public DynamicProjectBranchTabsProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new DynamicProjectBranchTabsProperty(formData.getString("branchTabs"));
        }
    }
}
