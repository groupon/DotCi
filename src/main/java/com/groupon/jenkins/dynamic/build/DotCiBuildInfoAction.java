/*
The MIT License (MIT)

Copyright (c) 2016, Groupon, Inc.

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

import com.google.common.base.Joiner;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import hudson.model.Run;
import jenkins.model.RunAction2;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DotCiBuildInfoAction implements RunAction2 {
    private List<String> commands;
    private String buildConfiguration;

    public DotCiBuildInfoAction() {
        this.commands = new ArrayList<>();
        this.buildConfiguration = "";
    }

    public DotCiBuildInfoAction(final ShellCommands commands) {
        this();
        this.commands = new ArrayList<>(commands.getCommands());

    }

    public DotCiBuildInfoAction(final String buildConfiguration) {
        this();
        this.buildConfiguration = buildConfiguration;
    }

    public String getBuildConfiguration() {
        return this.buildConfiguration;
    }

    public void setBuildConfiguration(final String buildConfiguration) {
        this.buildConfiguration = buildConfiguration;

    }

    public String getScript() {
        return this.commands == null ? "" : Joiner.on("\n").join(this.commands);
    }

    public void doBuildScript(final StaplerRequest req, final StaplerResponse rsp) throws ServletException, IOException {
        final InputStream file = new ByteArrayInputStream(getScript().getBytes(StandardCharsets.UTF_8));
        rsp.serveFile(req, file, new Date().getTime(), 0, -1L, "dotci.sh");
    }

    @Override
    public void onAttached(final Run<?, ?> r) {

    }

    @Override
    public void onLoad(final Run<?, ?> r) {

    }

    @Override
    public String getIconFileName() {
        return "/plugin/DotCi/images/24x24/logo.png";
    }

    @Override
    public String getDisplayName() {
        return "DotCi Build Info";
    }

    @Override
    public String getUrlName() {
        return "dotCiBuildInfo";
    }

    public void addCommands(final ShellCommands commands) {
        this.commands.addAll(commands.getCommands());
    }
}
