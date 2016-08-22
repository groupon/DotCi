package com.groupon.jenkins.dynamic.build;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Hudson;
import hudson.model.ProminentProjectAction;
import jenkins.model.TransientActionFactory;
import org.kohsuke.stapler.Stapler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

@Extension
public class ToggleNewUIAction extends TransientActionFactory<DynamicProject> implements ProminentProjectAction {
    @Override
    public String getIconFileName() {
        return "document.png" ;
    }

    @Override
    public String getDisplayName() {
        return "New UI (Beta)";
    }

    @Override
    public String getUrlName() {
        return "toggleNewUI";
    }

    @Override
    public Class type() {
        return DynamicProject.class;
    }

    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull DynamicProject target) {
        return Arrays.asList( new ToggleNewUIAction());
    }


}
