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

import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import hudson.Functions;
import hudson.model.BuildListener;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.tasks.Shell;
import java.io.IOException;

public class ShellScriptRunner {
    private BuildExecutionContext buildExecutionContext;
    private BuildListener listener;

    public ShellScriptRunner(BuildExecutionContext buildExecutionContext, BuildListener listener){
        this.buildExecutionContext = buildExecutionContext;
        this.listener = listener;

    }
    public Result runScript(ShellCommands commands) throws IOException, InterruptedException {
        Result r = Result.FAILURE;
        //Todo: use VitualChannel to figure out OS
        String shellInterpreter = Functions.isWindows() ? "sh" : "/bin/bash";
        try {
            Shell execution = new Shell("#!"+shellInterpreter+ " -le \n" + commands.toShellScript());
            if (buildExecutionContext.performStep(execution, listener)) {
                r = Result.SUCCESS;
            }
        } catch (InterruptedException e) {
            r = Executor.currentExecutor().abortResult();
            throw e;
        } finally {
            buildExecutionContext.setResult(r);
        }
        return r;
    }

}
