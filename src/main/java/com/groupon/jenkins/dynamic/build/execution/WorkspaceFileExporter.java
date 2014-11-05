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
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Messages;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

public class WorkspaceFileExporter {
    private final String contents;
	private final String fileName;
    private String permissions;

    public WorkspaceFileExporter(String fileName, String contents, String permissions) {
        this.contents = contents;
		this.fileName = fileName;
        this.permissions = permissions;
    }


	public FilePath export(AbstractBuild<?, ?> build, TaskListener listener) throws InterruptedException, IOException {
		FilePath ws = build.getWorkspace();
		if (ws == null) {
			Node node = build.getBuiltOn();
			if (node == null) {
				throw new NullPointerException("no such build node: " + build.getBuiltOnStr());
			}
			throw new NullPointerException("no workspace from node " + node + " which is computer " + node.toComputer() + " and has channel " + node.getChannel());
		}
		try {
			return createScriptFile(ws, contents);
		} catch (IOException e) {
			Util.displayIOException(e, listener);
			e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_UnableToProduceScript()));
            throw e;
		}

	}

	private FilePath createScriptFile(FilePath ws, final String fileContents) throws IOException, InterruptedException {
		return new FilePath(ws.getChannel(), ws.act(new WorkspaceFileCreatorFileCallable(fileName,fileContents, permissions)));
	}
    private static class WorkspaceFileCreatorFileCallable implements FilePath.FileCallable<String> {
        private final String fileContents;
        private static final long serialVersionUID = 1L;
        private final String fileName;
        private String permissions;

        WorkspaceFileCreatorFileCallable(String fileName, String fileContents, String permissions) {
            this.fileContents = fileContents;
            this.fileName = fileName;
            this.permissions = permissions;
        }
        @Override
        public String invoke(File dir, VirtualChannel channel) throws IOException {
            dir.mkdirs();
            File f;
            Writer w = null;
            try {
                f = new File(dir.getAbsolutePath() + "/" + fileName);
                f.createNewFile();
                w = new FileWriter(f);
                w.write(fileContents);
                Files.setPosixFilePermissions(f.toPath(), PosixFilePermissions.fromString(permissions));
            } finally {
                if (w != null) {
                    w.close();
                }
            }
            return f.getAbsolutePath();
        }
    }
}