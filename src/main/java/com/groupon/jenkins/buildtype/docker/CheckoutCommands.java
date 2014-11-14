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

package com.groupon.jenkins.buildtype.docker;

import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import com.groupon.jenkins.github.GitUrl;
import java.util.Map;
import static java.lang.String.format;

public class CheckoutCommands {
    public static ShellCommands get(Map<String, Object> dotCiEnvVars) {
        GitUrl gitRepoUrl = new GitUrl((String) dotCiEnvVars.get("GIT_URL"));
        String gitUrl = gitRepoUrl.getHttpsUrl();
        String checkoutLocation = format("/var/%s",gitRepoUrl.getFullRepoName());
        ShellCommands shellCommands = new ShellCommands();
        if(dotCiEnvVars.get("DOTCI_PULL_REQUEST") != null){
            shellCommands.add(format("git clone  --depth=50 %s %s",gitUrl,checkoutLocation));
            shellCommands.add("cd " + checkoutLocation);
            shellCommands.add(format("git fetch origin \"+refs/pull/%s/merge:\"", dotCiEnvVars.get("DOTCI_PULL_REQUEST")));
            shellCommands.add("git reset --hard FETCH_HEAD");
        }else {
            shellCommands.add(format("git clone  --branch=%s %s %s", dotCiEnvVars.get("DOTCI_BRANCH"), gitUrl,checkoutLocation));
            shellCommands.add("cd " + checkoutLocation);
            shellCommands.add(format("git reset --hard  %s", dotCiEnvVars.get("SHA")));
        }
        return shellCommands;
    }
}
