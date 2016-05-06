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
package com.groupon.jenkins.github;

import com.google.common.io.CharStreams;
import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import com.groupon.jenkins.github.services.GithubHookShotRepository;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterValue;
import hudson.model.UnprotectedRootAction;
import hudson.security.ACL;
import hudson.util.SequentialExecutionQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;

@Extension
@ExportedBean
public class GithubWebhook implements UnprotectedRootAction {
    private static final Logger LOGGER = Logger.getLogger(GithubWebhook.class.getName());


    @Override
    public String getUrlName() {
        return "githook";
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void doIndex(StaplerRequest req, StaplerResponse response) throws IOException {
        String payload = req.getParameter("payload");
        if (StringUtils.isEmpty(payload) && "POST".equalsIgnoreCase(req.getMethod())) {
            payload = getRequestPayload(req);
        }
        if (StringUtils.isEmpty(payload)) {
            throw new IllegalArgumentException("Not intended to be browsed interactively (must specify payload parameter)");
        }


        if (SetupConfig.get().isMaster() ) {
            GithubHookShotRepository hookshotRepo = SetupConfig.get().getGithubHookShotRepository();
            hookshotRepo.saveHookShot(payload);
        }
    }

    protected String getRequestPayload(StaplerRequest req) throws IOException {
        return CharStreams.toString(req.getReader());
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

}
