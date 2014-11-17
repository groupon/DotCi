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
import java.io.IOException;
import org.apache.commons.lang.StringUtils;

public abstract class DotCiPluginAdapter extends DotCiExtension{
    protected String pluginInputFiles;
    private final String name;
    protected Object options;

    protected DotCiPluginAdapter(String name, String pluginInputFiles) {
        this.name = name;
        this.pluginInputFiles = pluginInputFiles;

    }

    public void setOptions(Object options) {
        this.options = options;
    }

    public String getName() {
        return this.name;
    }

    public abstract boolean perform(DynamicBuild dynamicBuild, Launcher launcher, BuildListener listener);

    public void runFinished(DynamicSubBuild run, DynamicBuild parent, BuildListener listener) throws IOException {
        if(StringUtils.isNotEmpty(pluginInputFiles)){
            copyFiles(run, parent, pluginInputFiles, listener);
        }
    }

     public void copyFiles(DynamicSubBuild run, DynamicBuild parent, String outputFiles, BuildListener listener) throws IOException {
        String baseWorkSpace;
        try {
            listener.getLogger().println("Copying files :" + outputFiles);
            FilePath workspacePath = run.getWorkspace();
            FilePath targetPath = parent.getWorkspace();
            if (workspacePath != null && targetPath != null) {
                baseWorkSpace = workspacePath.toURI().toString();
                for (FilePath file : workspacePath.list(outputFiles)) {
                    String dir = file.toURI().toString().replaceAll(baseWorkSpace, "").replaceAll(file.getName(), "");
                    FilePath targetChildDir = targetPath.child(dir);
                    if (!targetChildDir.exists()) {
                        targetChildDir.mkdirs();
                    }
                    file.copyTo(targetChildDir.child(file.getName()));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
