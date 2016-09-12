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
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.cause.GithubLogEntry;
import com.groupon.jenkins.mongo.JenkinsMapper;
import com.mongodb.DBObject;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mongodb.morphia.mapping.Mapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class PushAndPullRequestPayloadTest {

    @Test
    public void testProjectPushCause() throws IOException {
        final String payloadReq = readFile("push.json");
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(payloadReq);
        assertEquals("Started by GitHub push by surya", payload.getCause().getShortDescription());
    }

    @Test
    public void testProjectPushBranch() throws IOException {
        final String payloadReq = readFile("push.json");
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(payloadReq);
        assertEquals("master", payload.getBranch());
    }

    @Test
    public void testProjectPullRequestCause() throws IOException {
        final String payloadReq = readFile("pull_request.json");
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(payloadReq);
        assertEquals("Started by Github pull request  suryagroupon:master( Number : 4)", payload.getCause().getShortDescription());
    }

    @Test
    public void testProjectPullRequestUnmergableBranch() throws IOException {
        final String payloadReq = readFile("pull_request.json");
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(payloadReq);
        assertEquals("Pull Request: 4", payload.getBranch());
    }

    @Test
    public void testProjectPushDeleteBranch() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push_delete.json"));
        assertFalse(payload.needsBuild(getProject()));
    }

    @Test
    public void pullRequestOpenedShouldTriggerABuild() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request_opened.json"));
        assertTrue(payload.needsBuild(getProject()));
    }

    @Test
    public void pullRequestReopenedShouldTriggerABuild() throws IOException {
        final String payloadReq = readFile("pull_request_reopened.json");
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(payloadReq);
        assertTrue(payload.needsBuild(getProject()));
    }

    @Test
    public void pullRequestSynchronizeShouldTriggerABuild() throws IOException {
        final String payloadReq = readFile("pull_request_synchronized.json");
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(payloadReq);
        assertTrue(payload.needsBuild(getProject()));
    }

    private DynamicProject getProject() {
        return Mockito.mock(DynamicProject.class);
    }

    @Test
    public void pullRequestLabeledShouldNotTriggerABuild() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request_labeled.json"));
        assertFalse(payload.needsBuild(getProject()));
    }

    @Test
    public void testNonDeletePushShouldTriggerAbuild() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push.json"));
        assertTrue(payload.needsBuild(getProject()));
    }

    @Test
    public void pullRequestFromForkShouldTriggerABuild() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request_from_fork.json"));
        assertTrue(payload.needsBuild(getProject()));
    }

    @Test
    public void closedPullRequestFromForkShouldNotTriggerABuild() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request_from_fork_closed.json"));
        assertFalse(payload.needsBuild(getProject()));
    }

    @Test
    public void shaForPush() throws IOException {
        final String payloadReq = readFile("push.json");
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(payloadReq);
        assertEquals("c2496e3fcc9a55b0a9f9318563a81aa2f26f4047", payload.getSha());
    }

    @Test
    public void shaForPullRequest() throws IOException {
        final String payloadReq = readFile("pull_request_from_fork.json");
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(payloadReq);
        assertEquals("01923503d64d52cfd5af76fe66367b66532149e6", payload.getSha());
    }

    @Test
    public void diffUrlForPush() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push.json"));
        assertEquals("https://github.acme.com/surya/mycoolapp/compare/7974a8062f45...c2496e3fcc9a", payload.getDiffUrl());
    }

    @Test
    public void diffUrlForPullRequest() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request.json"));
        assertEquals("https://github.com/suryagaddipati/cancan/pull/4", payload.getDiffUrl());
    }

    @Test
    public void pusherForPush() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push.json"));
        assertEquals("surya", payload.getPusher());
    }

    @Test
    public void pusherPullRequest() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request.json"));
        assertEquals("suryagroupon", payload.getPusher());
    }

    @Test
    public void branchDescriptionForPush() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push.json"));
        assertEquals("master", payload.getBranchDescription());
    }

    @Test
    public void branchDescriptionForPullRequest() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request.json"));
        assertEquals("Pull Request 4", payload.getBranchDescription());
    }

    @Test
    public void should_not_build_tags() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push_tags.json"));
        Assert.assertFalse(payload.needsBuild(getProject()));
    }

    @Test
    public void should_build_tags_if_specified_in_project_config() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push_tags.json"));
        final DynamicProject project = getProject();
        when(project.shouldBuildTags()).thenReturn(true);
        Assert.assertTrue(payload.needsBuild(project));
    }

    @Test
    public void should_have_log_entries_for_push() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push.json"));
        final Iterable<GithubLogEntry> logEntries = payload.getLogEntries();
        assertFalse(Iterables.isEmpty(logEntries));
        assertEquals(1, Iterables.size(logEntries));
        final GithubLogEntry logEntry = Iterables.get(logEntries, 0);
        assertEquals("c2496e3fcc9a55b0a9f9318563a81aa2f26f4047", logEntry.getCommitId());
        assertNotNull(logEntry.getAffectedPaths());
        assertTrue(logEntry.getAffectedPaths().contains("README.md"));
    }

    @Test
    public void should_not_have_log_entries_for_pull_request() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request.json"));
        assertTrue(Iterables.isEmpty(payload.getLogEntries()));
    }

    @Test
    public void should_get_project_url_from_payload() throws IOException {
        PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request.json"));
        assertEquals("https://github.com/suryagaddipati/cancan", payload.getProjectUrl());

        payload = new PushAndPullRequestPayload(readFile("push.json"));
        assertEquals("https://github.acme.com/surya/mycoolapp", payload.getProjectUrl());
    }

    @Test
    public void should_save_commits_in_cause() throws IOException {
        final Mapper mapper = new JenkinsMapper(this.getClass().getClassLoader());
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push_new_branch.json"));
        final DBObject dbObject = mapper.toDBObject(payload.getCause());
        assertNotNull("Log Entries not saved", dbObject.get("logEntries"));
        assertTrue(dbObject.get("logEntries") instanceof List);
    }

    @Test
    public void should_get_committer_name_from_push_payload() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push.json"));
        assertEquals("Surya Gaddipati", payload.getCommitterName());
    }

    @Test
    public void should_get_committer_name_from_pr_payload() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request.json"));
        assertEquals("suryagroupon", payload.getCommitterName());
    }

    @Test
    public void should_get_email_from_push_payload() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push.json"));
        assertEquals("sgaddipati@example.com", payload.getCommitterEmail());
    }

    @Test
    public void should_get_avatar_url_from_pr_payload() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request.json"));
        assertEquals("https://avatars.githubusercontent.com/u/6909085?", payload.getAvatarUrl());
    }

    @Test
    public void should_get_commit_message_from_push_payload() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push.json"));
        assertEquals("new commit", payload.getCommitMessage());
    }

    @Test
    public void should_get_commit_message_from_pr_payload_description() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request.json"));
        assertEquals("Update .ci.yml", payload.getCommitMessage());
    }

    @Test
    public void should_get_base_commit_sha_for_parent_sha_in_a_pr() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("pull_request.json"));
        assertEquals("2e8f55ee8489308b6d3b3e6d640774c3b857c9f5", payload.getParentSha());
    }

    @Test
    public void should_get_previous_commit_sha_for_parent_sha_for_push() throws IOException {
        final PushAndPullRequestPayload payload = new PushAndPullRequestPayload(readFile("push.json"));
        assertEquals("7974a8062f45ecab27387b763bd39277f3ba5aac", payload.getParentSha());
    }

    private String readFile(final String fileName) throws IOException {
        final InputStream stream = getClass().getResourceAsStream("/" + fileName);
        final String payloadReq = IOUtils.toString(stream);
        return payloadReq;
    }


}
