package com.groupon.jenkins;

import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import hudson.Extension;
import hudson.Plugin;
import jenkins.model.Jenkins;

import java.util.List;

@Extension
public class DotCiPlugin extends Plugin{
    @Override
    public void postInitialize() throws Exception {
        List<OrganizationContainer> orgs = SetupConfig.get().getOrganizationContainerRepository().getOrganizations();
        for(OrganizationContainer org : orgs){
            Jenkins.getActiveInstance().add(org, org.getName());
        }
    }
}
