package com.groupon.jenkins.github;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import com.groupon.jenkins.github.services.GithubHookShotRepository;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PeriodicWork;
import hudson.model.Queue;
import hudson.model.StringParameterValue;
import hudson.security.ACL;
import hudson.util.SequentialExecutionQueue;
import jenkins.model.Jenkins;
import org.acegisecurity.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Extension
public class GithubHookShotProcessorCron extends PeriodicWork {
    private static final Logger LOGGER = Logger.getLogger(GithubHookShotProcessorCron.class.getName());
    private final SequentialExecutionQueue queue = new SequentialExecutionQueue(Executors.newSingleThreadExecutor());
    @Override
    public long getRecurrencePeriod() {
        return 10*1000;
    }

    @Override
    protected void doRun() throws Exception {
        SetupConfig setupConfig = SetupConfig.get();
        if(setupConfig.isAgent()){

            GithubHookShotRepository hookShotRepo = setupConfig.getGithubHookShotRepository();
            String payload = hookShotRepo.getNextHookShot();
            if(payload != null){
              processGitHubPayload(payload);
            }
        }
    }

    public void processGitHubPayload(String payloadData) {
        SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
        final Payload payload = makePayload(payloadData);
        LOGGER.info("Received POST by " + payload.getPusher());
        LOGGER.info("Received kicking off build for " + payload.getProjectUrl());
        for (final DynamicProject job : makeDynamicProjectRepo().getJobsFor(payload.getProjectUrl())) {

            if (payload.needsBuild(job.shouldBuildTags())) {
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
}
