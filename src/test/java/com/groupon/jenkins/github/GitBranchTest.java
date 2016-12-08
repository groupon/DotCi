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
package com.groupon.jenkins.github;

import com.groupon.jenkins.git.GitBranch;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GitBranchTest {

    @Test
    public void regular_branches_are_not_pull_requests() {
        assertFalse(new GitBranch("master").isPullRequest());
    }

    @Test
    public void pull_request_branches_follow_convention() {
        assertTrue(new GitBranch("Pull Request: 27").isPullRequest());
    }

    @Test
    public void should_extract_pull_request_number() {
        assertEquals(27, new GitBranch("Pull Request: 27").pullRequestNumber());
    }

    @Test
    public void should_return_null_if_not_pull_request_when_asked_for_pull_request_number() {
        assertFalse(new GitBranch("master").isPullRequest());
    }

    @Test
    public void should_return_pr_number_if_not_pull_request_when_asked_for_pull_request_number() {
        assertEquals(27, new GitBranch("Pull Request: 27").pullRequestNumber());
    }

}
