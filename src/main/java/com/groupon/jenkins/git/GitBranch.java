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
package com.groupon.jenkins.git;

import com.google.common.base.Objects;
import com.groupon.jenkins.ForBackwardCompatiblity;

public class GitBranch {
    public transient static final String PULL_REQUEST_PREFIX = "Pull Request:";
    @ForBackwardCompatiblity
    private transient final String PULL_REQUEST_PATTERN = null;
    private final String branch;


    public GitBranch(final String branch) {
        this.branch = branch;
    }

    public int pullRequestNumber() {
        final String number = this.branch.replace(PULL_REQUEST_PREFIX, "").trim();
        return Integer.parseInt(number);
    }

    public boolean isPullRequest() {
        return this.branch.startsWith(PULL_REQUEST_PREFIX);
    }

    @Override
    public String toString() {
        return this.branch;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.branch);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final GitBranch other = (GitBranch) obj;
        return Objects.equal(this.branch, other.branch);
    }

}
