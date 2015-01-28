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

import java.util.ArrayList;
import java.util.List;

public class BuildHistoryTab {

    private boolean active;
    private String url;
    private String font;
    private String state;
    private String name;
    private boolean removable;
    private static BuildHistoryTab getAll(){
        return new BuildHistoryTab("all","fa fa-users","grey","All", false);
    }

    private static BuildHistoryTab getMine(){
        return new BuildHistoryTab("mine","octicon octicon-person","grey","mine", false);
    }

    private static BuildHistoryTab getBranch(String branch){
        return new BuildHistoryTab(branch,"octicon octicon-git-branch","grey",branch, true);
    }
    public BuildHistoryTab(String url, String font, String state, String name, boolean removable) {
        this.url = url;
        this.font = font;
        this.state = state;
        this.name = name;
        this.removable = removable;
    }

    public boolean isActive(){
        return active;
    }
    public String getUrl(){
        return url;
    }
    public String getFont(){
        return font;
    }

    public String getState(){
        return state;
    }
    public String getName(){
        return name;
    }
    public boolean isRemovable(){
        return removable;
    }

    public static Iterable<BuildHistoryTab> getTabs(List<String> branches) {
        ArrayList<BuildHistoryTab> tabs = new ArrayList<BuildHistoryTab>();
        tabs.add(getAll());
        tabs.add(getMine());
        for(String branch:branches){
            tabs.add(getBranch(branch));
        }
        return tabs;
    }

    public void setActive() {
       this.active =true;
    }
}
