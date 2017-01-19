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

package com.groupon.jenkins.buildtype.util.shell;

import com.groupon.jenkins.dynamic.build.DotCiBuildInfoAction;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import hudson.Functions;
import hudson.model.BuildListener;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.Shell;

import java.io.IOException;

public class ShellScriptRunner {
    private final BuildExecutionContext buildExecutionContext;
    private final BuildListener listener;

    public ShellScriptRunner(final BuildExecutionContext buildExecutionContext, final BuildListener listener) {
        this.buildExecutionContext = buildExecutionContext;
        this.listener = listener;

    }

    public Result runScript(final ShellCommands commands) throws IOException, InterruptedException {
        Result r = Result.FAILURE;
        //Todo: use VitualChannel to figure out OS
        final String shellInterpreter = Functions.isWindows() ? "sh" : "/bin/bash";
        final Run run = this.buildExecutionContext.getRun();
        addExecutionInfoAction(run, commands);
        try {
            final Shell execution = new Shell("#!" + shellInterpreter + " -le \n" + commands.toShellScript());
            if (this.buildExecutionContext.performStep(execution, this.listener)) {
                r = Result.SUCCESS;
            }
        } catch (final InterruptedException e) {
            r = Executor.currentExecutor().abortResult();
            throw e;
        } finally {
            this.buildExecutionContext.setResult(r);
        }
        return r;
    }

    private void addExecutionInfoAction(final Run run, final ShellCommands commands) throws IOException {
        final DotCiBuildInfoAction dotCiBuildInfoAction = run.getAction(DotCiBuildInfoAction.class);
        if (dotCiBuildInfoAction == null) {
            run.addAction(new DotCiBuildInfoAction(commands));
        } else {
            dotCiBuildInfoAction.addCommands(commands);
        }
        run.save();
    }

}
