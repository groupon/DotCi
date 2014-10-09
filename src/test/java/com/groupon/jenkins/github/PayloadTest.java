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

import com.google.common.collect.Iterables;
import com.groupon.jenkins.dynamic.build.cause.GithubLogEntry;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class PayloadTest {

    @Test
    public void testProjectPushCause() throws IOException {
        String payloadReq = readFile("push.json");
        Payload payload = new Payload(payloadReq);
        assertEquals("Started by GitHub push by surya", payload.getCause().getShortDescription());
    }

    @Test
    public void testProjectPushBranch() throws IOException {
        String payloadReq = readFile("push.json");
        Payload payload = new Payload(payloadReq);
        assertEquals("master", payload.getBranch());
    }

    @Test
    public void testProjectPullRequestCause() throws IOException {
        String payloadReq = readFile("pull_request.json");
        Payload payload = new Payload(payloadReq);
        assertEquals("Started by Github pull request  suryagroupon:master( Number : 4)", payload.getCause().getShortDescription());
    }

    @Test
    public void testProjectPullRequestUnmergableBranch() throws IOException {
        String payloadReq = readFile("pull_request.json");
        Payload payload = new Payload(payloadReq);
        assertEquals("Pull Request: 4", payload.getBranch());
    }

    @Test
    public void testProjectPushDeleteBranch() throws IOException {
        Payload payload = new Payload(readFile("push_delete.json"));
        assertFalse(payload.needsBuild());
    }

    @Test
    public void testNonDeletePushShouldTriggerAbuild() throws IOException {
        Payload payload = new Payload(readFile("push.json"));
        assertTrue(payload.needsBuild());
    }

    @Test
    public void pullRequestFromTheSameRepoShouldNotTriggerABuild() throws IOException {
        String payloadReq = readFile("pull_request_from_the_same_repo.json");
        Payload payload = new Payload(payloadReq);
        assertFalse(payload.needsBuild());
    }

    @Test
    public void pullRequestFromForkShouldTriggerABuild() throws IOException {
        Payload payload = new Payload(readFile("pull_request_from_fork.json"));
        assertTrue(payload.needsBuild());
    }

    @Test
    public void closedPullRequestFromForkShouldNotTriggerABuild() throws IOException {
        Payload payload = new Payload(readFile("pull_request_from_fork_closed.json"));
        assertFalse(payload.needsBuild());
    }

    @Test
    public void shaForPush() throws IOException {
        String payloadReq = readFile("push.json");
        Payload payload = new Payload(payloadReq);
        assertEquals("c2496e3fcc9a55b0a9f9318563a81aa2f26f4047", payload.getSha());
    }

    @Test
    public void shaForPullRequest() throws IOException {
        String payloadReq = readFile("pull_request_from_fork.json");
        Payload payload = new Payload(payloadReq);
        assertEquals("01923503d64d52cfd5af76fe66367b66532149e6", payload.getSha());
    }

    @Test
    public void diffUrlForPush() throws IOException {
        Payload payload = new Payload(readFile("push.json"));
        assertEquals("https://github.acme.com/surya/mycoolapp/compare/7974a8062f45...c2496e3fcc9a", payload.getDiffUrl());
    }

    @Test
    public void diffUrlForPullRequest() throws IOException {
        Payload payload = new Payload(readFile("pull_request.json"));
        assertEquals("https://github.com/suryagaddipati/cancan/pull/4", payload.getDiffUrl());
    }

    @Test
    public void pusherForPush() throws IOException {
        Payload payload = new Payload(readFile("push.json"));
        assertEquals("surya", payload.getPusher());
    }

    @Test
    public void pusherPullRequest() throws IOException {
        Payload payload = new Payload(readFile("pull_request.json"));
        assertEquals("suryagroupon", payload.getPusher());
    }

    @Test
    public void branchDescriptionForPush() throws IOException {
        Payload payload = new Payload(readFile("push.json"));
        assertEquals("master", payload.getBranchDescription());
    }

    @Test
    public void branchDescriptionForPullRequest() throws IOException {
        Payload payload = new Payload(readFile("pull_request.json"));
        assertEquals("Pull Request 4", payload.getBranchDescription());
    }

    @Test
    public void should_not_build_tags() throws IOException {
        Payload payload = new Payload(readFile("push_tags.json"));
        Assert.assertFalse(payload.needsBuild());
    }

    @Test
    public void should_have_log_entries_for_push() throws IOException {
        Payload payload = new Payload(readFile("push.json"));
        Iterable<GithubLogEntry> logEntries = payload.getLogEntries();
        assertFalse(Iterables.isEmpty(logEntries));
        assertEquals(1, Iterables.size(logEntries));
        GithubLogEntry logEntry = Iterables.get(logEntries, 0);
        assertEquals("c2496e3fcc9a55b0a9f9318563a81aa2f26f4047", logEntry.getCommitId());
    }

    @Test
    public void should_not_have_log_entries_for_pull_request() throws IOException {
        Payload payload = new Payload(readFile("pull_request.json"));
        assertTrue(Iterables.isEmpty(payload.getLogEntries()));
    }

    @Test
    public void should_get_project_url_from_payload() throws IOException {
        Payload payload = new Payload(readFile("pull_request.json"));
        assertEquals("https://github.com/suryagaddipati/cancan", payload.getProjectUrl());

        payload = new Payload(readFile("push.json"));
        assertEquals("https://github.acme.com/surya/mycoolapp", payload.getProjectUrl());
    }

    private String readFile(String fileName) throws IOException {
        InputStream stream = getClass().getResourceAsStream("/" + fileName);
        String payloadReq = IOUtils.toString(stream);
        return payloadReq;
    }

}
