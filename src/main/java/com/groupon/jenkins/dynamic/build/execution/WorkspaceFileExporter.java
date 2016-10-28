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

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Builder;
import hudson.tasks.Messages;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

public class WorkspaceFileExporter extends Builder {

    private final WorkspaceFile workspaceFile;
    private final Operation operation;

    public WorkspaceFileExporter(final WorkspaceFile workspaceFile, final Operation operation) {
        this.workspaceFile = workspaceFile;
        this.operation = operation;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        return Operation.DELETE.equals(this.operation) ? delete(build) : export(build, listener);
    }

    private boolean export(final AbstractBuild<?, ?> build, final TaskListener listener) throws InterruptedException, IOException {
        final FilePath ws = getFilePath(build);
        try {
            createFile(ws);
        } catch (final IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_UnableToProduceScript()));
            throw e;
        }
        return true;

    }

    private boolean delete(final AbstractBuild<?, ?> build) throws IOException, InterruptedException {
        final FilePath ws = getFilePath(build);
        ws.act(new WorkspaceFileDeleterFileCallable(this.workspaceFile));
        return true;
    }

    private FilePath getFilePath(final AbstractBuild<?, ?> build) {
        final FilePath ws = build.getWorkspace();
        if (ws == null) {
            final Node node = build.getBuiltOn();
            if (node == null) {
                throw new RuntimeException("no such build node: " + build.getBuiltOnStr());
            }
            throw new RuntimeException("no workspace from node " + node + " which is computer " + node.toComputer() + " and has channel " + node.getChannel());
        }
        return ws;
    }

    private FilePath createFile(final FilePath ws) throws IOException, InterruptedException {
        return new FilePath(ws.getChannel(), ws.act(new WorkspaceFileCreatorFileCallable(this.workspaceFile)));
    }

    public enum Operation {DELETE, CREATE}

    public static class WorkspaceFile implements Serializable {
        private static final long serialVersionUID = 1L;
        final String contents;
        final String fileName;
        final String permissions;

        public WorkspaceFile(final String fileName, final String contents, final String permissions) {
            this.contents = contents;
            this.fileName = fileName;
            this.permissions = permissions;
        }

        public WorkspaceFile(final String fileName) {
            this.fileName = fileName;
            this.contents = null;
            this.permissions = null;
        }

    }

    private static class WorkspaceFileDeleterFileCallable implements FilePath.FileCallable<String> {
        private static final long serialVersionUID = 1L;
        private final WorkspaceFile workspaceFile;

        public WorkspaceFileDeleterFileCallable(final WorkspaceFile workspaceFile) {
            this.workspaceFile = workspaceFile;
        }

        public String invoke(final File dir, final VirtualChannel channel) throws IOException {
            final File f = new File(dir.getAbsolutePath() + "/" + this.workspaceFile.fileName);
            f.delete();
            return f.getAbsolutePath();
        }

        @Override
        public void checkRoles(final RoleChecker roleChecker) throws SecurityException {

        }
    }

    private static class WorkspaceFileCreatorFileCallable implements FilePath.FileCallable<String> {
        private static final long serialVersionUID = 1L;
        private final WorkspaceFile workspaceFile;

        public WorkspaceFileCreatorFileCallable(final WorkspaceFile workspaceFile) {
            this.workspaceFile = workspaceFile;
        }

        @Override
        public String invoke(final File dir, final VirtualChannel channel) throws IOException {
            dir.mkdirs();
            File f;
            Writer w = null;
            try {
                f = new File(dir.getAbsolutePath() + "/" + this.workspaceFile.fileName);
                f.createNewFile();
                w = new FileWriter(f);
                w.write(this.workspaceFile.contents);
                Files.setPosixFilePermissions(f.toPath(), PosixFilePermissions.fromString(this.workspaceFile.permissions));
            } finally {
                if (w != null) {
                    w.close();
                }
            }
            return f.getAbsolutePath();
        }

        @Override
        public void checkRoles(final RoleChecker roleChecker) throws SecurityException {

        }
    }
}
