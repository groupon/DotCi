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
package com.groupon.jenkins.dynamic.buildtype;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.execution.BuildExecutionContext;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import jenkins.model.Jenkins;

import java.io.IOException;

public abstract class BuildType implements ExtensionPoint {
    public static BuildType newBuildType(final DynamicProject project) {
        final String requestedBuildType = getBuildType(project);
        for (final BuildType buildType : all()) {
            if (buildType.getId().equals(requestedBuildType)) {
                try {
                    return buildType.getClass().newInstance();
                } catch (final InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (final IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new IllegalStateException("Build Type not found for the build");
    }

    public static boolean isProjectOfBuildType(final DynamicProject project, final Class<? extends BuildType> clazz) {
        final String projectBuildType = getBuildType(project);
        return clazz.getName().equals(projectBuildType);
    }

    private static String getBuildType(final DynamicProject project) {
        return project.getBuildType() == null ? SetupConfig.get().getDefaultBuildType() : project.getBuildType();
    }

    public static ExtensionList<BuildType> all() {
        return Jenkins.getInstance().getExtensionList(BuildType.class);
    }

    public abstract String getDescription();

    public String getId() {
        return getClass().getName();
    }


    public abstract Result runBuild(DynamicBuild build, BuildExecutionContext buildExecutionContext, Launcher launcher, BuildListener listener) throws IOException, InterruptedException;

}
