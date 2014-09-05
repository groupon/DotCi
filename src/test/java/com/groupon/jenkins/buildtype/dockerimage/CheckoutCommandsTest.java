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

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import org.junit.Assert;
import org.junit.Test;

public class CheckoutCommandsTest {

    @Test
    public void should_checkout_pr_if_pr(){
       ShellCommands commands = CheckoutCommands.get(ImmutableMap.of("DOTCI_PULL_REQUEST", "17", "GIT_URL","git@github.com:groupon/DotCi.git")) ;
        Assert.assertEquals("git fetch origin \"+refs/pull/17/merge:\"",commands.get(2));
    }

    @Test
    public void should_checkout_branch_if_branch(){
        ShellCommands commands = CheckoutCommands.get(ImmutableMap.of("GIT_URL", "git@github.com:groupon/DotCi.git", "DOTCI_BRANCH","dotci_branch")) ;
        Assert.assertEquals("git clone  --branch=dotci_branch https://github.com/groupon/DotCi /var/groupon/DotCi",commands.get(0));
    }
}