package com.groupon.jenkins.extensions;

import com.groupon.jenkins.buildtype.plugins.*;
import com.groupon.jenkins.dynamic.build.*;
import hudson.*;
import hudson.model.*;
import hudson.tasks.*;
import jenkins.tasks.*;
import org.apache.commons.beanutils.*;
import org.jenkinsci.plugins.workflow.structs.DescribableHelper;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class GenericSimpleBuildStepPlugin extends DotCiPluginAdapter {
    private Descriptor<? extends  SimpleBuildStep> pluginDescriptor;
    private Object options;

    public GenericSimpleBuildStepPlugin(Descriptor<?> pluginDescriptor, Object options) {
        super("");
        this.pluginDescriptor = (Descriptor<? extends SimpleBuildStep>) pluginDescriptor;
        this.options = options;
    }


    @Override
    public boolean perform(DynamicBuild dynamicBuild, Launcher launcher, BuildListener listener) {
        SimpleBuildStep plugin = getPlugin();
        try {
            plugin.perform(dynamicBuild,launcher,listener);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private SimpleBuildStep getPlugin() {
        try {
            return DescribableHelper.instantiate(pluginDescriptor.clazz, (Map<String, ?>) options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
