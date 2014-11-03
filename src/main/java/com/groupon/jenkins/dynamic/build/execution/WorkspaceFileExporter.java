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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.tasks.Builder;
import hudson.tasks.Messages;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

public class WorkspaceFileExporter extends Builder {
	private final String contents;
	private final String fileName;

	public WorkspaceFileExporter(String fileName, String contents) {
		this.contents = contents;
		this.fileName = fileName;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		return perform(build, launcher, (TaskListener) listener);
	}

	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws InterruptedException {
		FilePath ws = build.getWorkspace();
		if (ws == null) {
			Node node = build.getBuiltOn();
			if (node == null) {
				throw new NullPointerException("no such build node: " + build.getBuiltOnStr());
			}
			throw new NullPointerException("no workspace from node " + node + " which is computer " + node.toComputer() + " and has channel " + node.getChannel());
		}
		FilePath script = null;
		try {
			script = createScriptFile(ws, contents);
		} catch (IOException e) {
			Util.displayIOException(e, listener);
			e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_UnableToProduceScript()));
			return false;
		}

		return true;
	}

	private FilePath createScriptFile(FilePath ws, final String dockerfileContents) throws IOException, InterruptedException {
		return new FilePath(ws.getChannel(), ws.act(new WorkspaceFileCreatorFileCallable(dockerfileContents, fileName)));
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<Builder> {
		@Override
		public Builder newInstance(StaplerRequest req, JSONObject data) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getDisplayName() {
			return "Docker";
		}
	}
}