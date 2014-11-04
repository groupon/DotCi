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

import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

final class WorkspaceFileCreatorFileCallable implements FileCallable<String> {
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