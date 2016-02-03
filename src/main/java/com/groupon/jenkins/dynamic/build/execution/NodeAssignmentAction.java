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
package com.groupon.jenkins.dynamic.build.execution;

import hudson.model.InvisibleAction;
import hudson.model.Label;
import hudson.model.labels.LabelAssignmentAction;
import hudson.model.queue.SubTask;
import org.mongodb.morphia.annotations.Transient;

import javax.annotation.Nonnull;

/**
 * Created by a on 12/28/15.
 */
public class NodeAssignmentAction extends InvisibleAction implements LabelAssignmentAction {

    @Transient
    private Label label;

    public NodeAssignmentAction(Label label) {
        super();
        this.label = label;
    }


    @Override
    public Label getAssignedLabel(@Nonnull SubTask subTask) {
        return label;
    }
}
