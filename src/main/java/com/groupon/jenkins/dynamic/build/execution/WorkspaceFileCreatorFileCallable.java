package com.groupon.jenkins.dynamic.build.execution;

import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

final class WorkspaceFileCreatorFileCallable implements FileCallable<String> {
	private final String dockerfileContents;
	private static final long serialVersionUID = 1L;
	private final String fileName;

	WorkspaceFileCreatorFileCallable(String dockerfileContents, String fileName) {
		this.dockerfileContents = dockerfileContents;
		this.fileName = fileName;
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
			w.write(dockerfileContents);
		} finally {
			if (w != null) {
				w.close();
			}
		}
		return f.getAbsolutePath();
	}
}