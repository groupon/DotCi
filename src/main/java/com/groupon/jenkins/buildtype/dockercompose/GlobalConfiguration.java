package com.groupon.jenkins.buildtype.dockercompose;

import hudson.Extension;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class GlobalConfiguration extends jenkins.model.GlobalConfiguration {
    public static final String DEFAULT_CLONE_URL_TEMPlATE = "https://<DOMAIN>/<ORG>/<REPO>.git";
    private String cloneUrlTemplate;


    public GlobalConfiguration() {
        load();
    }

    public static GlobalConfiguration get() {
        return jenkins.model.GlobalConfiguration.all().get(GlobalConfiguration.class);
    }

    public String getCloneUrlTemplate() {
        return StringUtils.isEmpty(cloneUrlTemplate) ? DEFAULT_CLONE_URL_TEMPlATE : cloneUrlTemplate;
    }

    public void setCloneUrlTemplate(String cloneUrlTemplate) {
        this.cloneUrlTemplate = cloneUrlTemplate;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
        req.bindJSON(this, json);
        save();
        return true;
    }
}
