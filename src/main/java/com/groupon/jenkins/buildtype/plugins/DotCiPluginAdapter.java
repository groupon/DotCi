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
package com.groupon.jenkins.buildtype.plugins;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicSubBuild;
import com.groupon.jenkins.extensions.DotCiExtension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public abstract class DotCiPluginAdapter extends DotCiExtension {
    private final String name;
    protected String pluginInputFiles;
    protected Object options;

    protected DotCiPluginAdapter(final String name, final String pluginInputFiles) {
        this.name = name;
        this.pluginInputFiles = pluginInputFiles;
    }

    protected DotCiPluginAdapter(final String name) {
        this.name = name;
    }

    public void setOptions(final Object options) {
        this.options = options;
    }

    public String getName() {
        return this.name;
    }

    public abstract boolean perform(DynamicBuild dynamicBuild, Launcher launcher, BuildListener listener);

    public void runFinished(final DynamicSubBuild run, final DynamicBuild parent, final BuildListener listener) throws IOException {
        if (StringUtils.isNotEmpty(getPluginInputFiles())) {
            copyFiles(run, parent, getPluginInputFiles(), listener);
        }
    }

    public String getPluginInputFiles() {
        return this.pluginInputFiles;
    }

    public void copyFiles(final DynamicSubBuild run, final DynamicBuild parent, final String outputFiles, final BuildListener listener) throws IOException {
        final String baseWorkSpace;
        try {
            listener.getLogger().println("Copying files :" + outputFiles);
            final FilePath workspacePath = run.getWorkspace();
            final FilePath targetPath = parent.getWorkspace();
            if (workspacePath != null && targetPath != null) {
                baseWorkSpace = workspacePath.toURI().toString();
                for (final FilePath file : workspacePath.list(outputFiles)) {
                    final String dir = file.toURI().toString().replaceAll(baseWorkSpace, "").replaceAll(file.getName(), "");
                    final FilePath targetChildDir = targetPath.child(dir);
                    if (!targetChildDir.exists()) {
                        targetChildDir.mkdirs();
                    }
                    file.copyTo(targetChildDir.child(file.getName()));
                }
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Collection<String> getValidationErrors() {
        return Collections.EMPTY_LIST;
    }
}
