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

package com.groupon.jenkins.buildtype.dockerimage;

import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import java.util.Map;

public class CheckoutCommands {
    public static ShellCommands get(Map<String, String> dotCiEnvVars) {
        ShellCommands shellCommands = new ShellCommands();
        shellCommands.add("find . -delete");
        if(dotCiEnvVars.get("DOTCI_PULL_REQUEST") != null){
            shellCommands.add("git init");
            shellCommands.add(String.format("git fetch origin \"+refs/pull/%s/merge:\"", dotCiEnvVars.get("DOTCI_PULL_REQUEST")));
            shellCommands.add("git reset --hard FETCH_HEAD");
        }else {
            shellCommands.add(String.format("git clone  --branch=%s %s .", dotCiEnvVars.get("DOTCI_BRANCH"), dotCiEnvVars.get("GIT_URL")));
            shellCommands.add(String.format("git reset --hard  %s", dotCiEnvVars.get("SHA")));
        }
        return shellCommands;
    }
}
