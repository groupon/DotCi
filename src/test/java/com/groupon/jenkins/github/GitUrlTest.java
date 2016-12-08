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

import com.groupon.jenkins.git.GitUrl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GitUrlTest {

    @Test
    public void should_get_full_name_from_url() {
        String fullName = new GitUrl("git@github.com:suryagaddipati/cancan.git").getFullRepoName();
        assertEquals("suryagaddipati/cancan", fullName);
    }

    @Test
    public void should_accept_dots_in_the_url() {
        String project = new GitUrl("git@github.acme.com:foo/bar.rb.git").getFullRepoName();
        assertEquals("foo/bar.rb", project);
    }

    @Test
    public void should_normalize_http_url() {
        String project = new GitUrl("https://github.acme.com/foo/bar.rb").getFullRepoName();
        assertEquals("foo/bar.rb", project);
    }

    @Test
    public void should_convert_http_url_into_ssh_url() {
        String projectUrl = new GitUrl("https://github.acme.com/foo/bar.rb").getUrl();
        assertEquals("git@github.acme.com:foo/bar.rb.git", projectUrl);
    }

    @Test
    public void should_convert_http_url_into_git_url() {
        String projectUrl = new GitUrl("https://github.com/groupon/DotCi").getGitUrl();
        assertEquals("git@github.com:groupon/DotCi.git", projectUrl);
    }

    @Test
    public void should_convert_git_url_into_https_url() {
        String projectUrl = new GitUrl("git@github.com:groupon/DotCi.git").getHttpsUrl();
        assertEquals("https://github.com/groupon/DotCi", projectUrl);
    }

    @Test
    public void should_apply_template() {
        String template = new GitUrl("git@github.com:suryagaddipati/cancan.git").applyTemplate("http://<DOMAIN>:<ORG>/<REPO>");
        assertEquals("http://github.com:suryagaddipati/cancan", template);
    }
}
