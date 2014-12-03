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
package com.groupon.jenkins.dynamic.build;

import com.groupon.jenkins.dynamic.build.cause.BuildCause;
import com.groupon.jenkins.dynamic.build.cause.GithubLogEntry;
import com.groupon.jenkins.dynamic.build.cause.ManualBuildCause;
import com.groupon.jenkins.dynamic.build.cause.UnknownBuildCause;
import com.groupon.jenkins.dynamic.build.commithistory.CommitHistoryView;
import com.groupon.jenkins.github.GitBranch;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import com.groupon.jenkins.testhelpers.DynamicBuildFactory;
import hudson.EnvVars;
import hudson.scm.ChangeLogSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;

import static com.groupon.jenkins.testhelpers.DynamicBuildFactory.newBuild;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DynamicBuildModelTest {

    @Test
    public void should_add_manual_build_cause_before_run_if_build_is_manually_kicked_off() {
        DynamicBuild dynamicBuild = DynamicBuildFactory.newBuild().manualStart("surya", "master").get();
        GithubRepositoryService githubRepositoryService = mock(GithubRepositoryService.class);
        when(githubRepositoryService.getShaForBranch("master")).thenReturn("masterSha");
        DynamicBuildModel model = new DynamicBuildModel(dynamicBuild, githubRepositoryService);
        model.run();
        verify(dynamicBuild).addCause(new ManualBuildCause(new GitBranch("master"), "masterSha", "surya"));
    }

    @Test
    public void should_add_commit_history_view_to_build() {
        DynamicBuild dynamicBuild = DynamicBuildFactory.newBuild().manualStart("surya", "master").get();
        GithubRepositoryService githubRepositoryService = mock(GithubRepositoryService.class);
        when(githubRepositoryService.getShaForBranch("master")).thenReturn("masterSha");
        DynamicBuildModel model = new DynamicBuildModel(dynamicBuild, githubRepositoryService);
        model.run();
        verify(dynamicBuild).addAction(any(CommitHistoryView.class));
    }


    @Test
    public void should_delete_sub_build_when_build_is_deleted() throws IOException {
        DynamicSubBuild subBuild = mock(DynamicSubBuild.class);
        DynamicBuild dynamicBuild = DynamicBuildFactory.newBuild().withSubBuilds(subBuild).get();
        GithubRepositoryService githubRepositoryService = mock(GithubRepositoryService.class);
        DynamicBuildModel model = new DynamicBuildModel(dynamicBuild, githubRepositoryService);

        model.deleteBuild();

        verify(dynamicBuild).delete();
        verify(subBuild).delete();
    }

    @Test
    public void should_return_null_cause_if_buildcause_has_not_been_initialized() {
        GithubRepositoryService githubRepositoryService = mock(GithubRepositoryService.class);
        DynamicBuildModel model = new DynamicBuildModel(newBuild().get(), githubRepositoryService);
        assertEquals(BuildCause.NULL_BUILD_CAUSE, model.getBuildCause());
    }

    @Test
    public void should_add_unknown_build_cause_if_build_kickedoff_by_an_upstream_build() {
        DynamicBuild dynamicBuild = DynamicBuildFactory.newBuild().get();
        when(dynamicBuild.getCause()).thenReturn(BuildCause.NULL_BUILD_CAUSE);
        GithubRepositoryService githubRepositoryService = mock(GithubRepositoryService.class);
        DynamicBuildModel model = new DynamicBuildModel(dynamicBuild, githubRepositoryService);
        model.run();
        ArgumentCaptor<UnknownBuildCause> argument = ArgumentCaptor.forClass(UnknownBuildCause.class);
        verify(dynamicBuild).addCause(argument.capture());
        UnknownBuildCause buildCause = argument.getValue();
        assertNotNull(buildCause);
    }

    @Test
    public void should_add_change_log_to_environment() throws IOException, InterruptedException {
        DynamicBuild dynamicBuild = DynamicBuildFactory.newBuild().get();
        DynamicBuildModel model = new DynamicBuildModel(dynamicBuild, null);
        when(dynamicBuild.getEnvironment(null)).thenReturn(new EnvVars());

        GithubLogEntry logEntry1 = new GithubLogEntry("message","github_url","commitId", Arrays.asList("Readme.md"));
        ChangeLogSet githubChangeLog= new GithubChangeLogSet(dynamicBuild,Arrays.asList(logEntry1));


        when(dynamicBuild.getChangeSet()).thenReturn(githubChangeLog);
        Map<String, Object> environment = model.getEnvironmentWithChangeSet(null);
        assertTrue(environment.containsKey("DOTCI_CHANGE_SET"));
        assertTrue(((List<String>)environment.get("DOTCI_CHANGE_SET")).contains("Readme.md"));
    }

}
