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
import com.groupon.jenkins.dynamic.build.YamlJobConfiguration;
import com.groupon.jenkins.dynamic.build.cause.GithubLogEntry;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import hudson.Extension;
import hudson.model.Descriptor;
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
import java.util.logging.Level;
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
    private final SequentialExecutionQueue queue = new SequentialExecutionQueue(Executors.newSingleThreadExecutor());

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
        processGitHubPayload(payload);
    }

    protected String getRequestPayload(StaplerRequest req) throws IOException {
        return CharStreams.toString(req.getReader());
    }

    public void processGitHubPayload(String payloadData) {
        SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
        final Payload payload = makePayload(payloadData);
        LOGGER.info("Received POST by " + payload.getPusher());
        LOGGER.info("Received kicking off build for " + payload.getProjectUrl());
        for (final DynamicProject job : makeDynamicProjectRepo().getJobsFor(payload.getProjectUrl())) {

            if (payload.needsBuild(job.shouldBuildTags())) {
                applyJobConfiguration(payload,job);
                LOGGER.info("starting job" + job.getName());
                queue.execute(new Runnable() {
                    @Override
                    public void run() {
                        job.scheduleBuild(0, payload.getCause(), new NoDuplicatesParameterAction(getParametersValues(job, payload.getBranch())));
                    }
                });
            }
        }
    }

    private void applyJobConfiguration(Payload payload, DynamicProject job) {
        for(GithubLogEntry logEntry: payload.getLogEntries()){
            if(logEntry.getAffectedPaths().contains(YamlJobConfiguration.CI_CONFIG_YML)){
                try {
                    new YamlJobConfiguration().apply(job);
                } catch (IOException e) {
                    LOGGER.log(Level.ALL,e.getMessage(), e);
                } catch (Descriptor.FormException e) {
                    LOGGER.log(Level.ALL,e.getMessage(), e);
                }
            }
        }
    }

    private List<ParameterValue> getParametersValues(Job job, String branch) {
        ParametersDefinitionProperty paramDefProp = (ParametersDefinitionProperty) job.getProperty(ParametersDefinitionProperty.class);
        ArrayList<ParameterValue> defValues = new ArrayList<ParameterValue>();

        for(ParameterDefinition paramDefinition : paramDefProp.getParameterDefinitions())
        {
            if("BRANCH".equals(paramDefinition.getName())){
                StringParameterValue branchParam = new StringParameterValue("BRANCH", branch);
                defValues.add(branchParam);
            }else{
                ParameterValue defaultValue  = paramDefinition.getDefaultParameterValue();
                if(defaultValue != null)
                    defValues.add(defaultValue);
            }
        }

        return defValues;
    }
    protected DynamicProjectRepository makeDynamicProjectRepo() {
        return SetupConfig.get().getDynamicProjectRepository();
    }

    protected Payload makePayload(String payloadData) {
        return new Payload(payloadData);
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
