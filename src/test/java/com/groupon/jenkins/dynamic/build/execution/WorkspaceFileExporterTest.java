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

package com.groupon.jenkins.dynamic.build.execution;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Shell;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;


public class WorkspaceFileExporterTest {
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void should_export_file_into_workspace_for_create_operation() throws IOException, InterruptedException, ExecutionException {
        final WorkspaceFileExporter createFile = new WorkspaceFileExporter(new WorkspaceFileExporter.WorkspaceFile("test.txt", "test Content", "rw-r--r--"), WorkspaceFileExporter.Operation.CREATE);
        final FreeStyleProject project = this.jenkinsRule.createFreeStyleProject();
        project.getBuildersList().add(createFile);
        project.getBuildersList().add(new Shell("ls -la"));
        project.getBuildersList().add(new Shell("cat test.txt"));
        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        final String output = FileUtils.readFileToString(build.getLogFile());
        assertThat(output).contains("test.txt");
        assertThat(output).contains("test Content");

    }

    @Test
    public void should_delete_file_from_workspace_for_delete_operation() throws IOException, InterruptedException, ExecutionException {
        final WorkspaceFileExporter deleteFile = new WorkspaceFileExporter(new WorkspaceFileExporter.WorkspaceFile("test.txt"), WorkspaceFileExporter.Operation.DELETE);
        final FreeStyleProject project = this.jenkinsRule.createFreeStyleProject();
        project.getBuildersList().add(new Shell("touch text.txt"));
        project.getBuildersList().add(deleteFile);
        final FreeStyleBuild build = project.scheduleBuild2(0).get();
        final String output = FileUtils.readFileToString(build.getLogFile());
        assertThat(output).doesNotContain("test.txt");

    }
}
