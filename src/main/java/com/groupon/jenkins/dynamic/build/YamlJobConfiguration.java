package com.groupon.jenkins.dynamic.build;

import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.listeners.ItemListener;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.stapler.Stapler;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class YamlJobConfiguration extends ItemListener {
    @Override
    public void onCreated(final Item item) {
        if (item instanceof DynamicProject) {
            try {
                apply((DynamicProject) item);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            } catch (final Descriptor.FormException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void apply(final DynamicProject job) throws IOException, Descriptor.FormException {
        try {
            final GHRepository repo = null;//job.dynamicBu
            final GHContent configFile = repo.getFileContent(".ci.config.yml", repo.getMasterBranch());
            final Yaml yaml = new Yaml();
            final Map<String, Object> jobConfig = (Map) yaml.load(configFile.read());

            final Map<String, Descriptor<?>> jobProperties = getDescriptors(JobProperty.class);

            for (final String key : jobConfig.keySet()) {
                final JobPropertyDescriptor descriptor = (JobPropertyDescriptor) (jobProperties.containsKey(key) ? jobProperties.get(key) : jobProperties.get(key + "Property"));
                if (descriptor != null && descriptor.isApplicable(job.getClass())) {
                    final JSONObject jsonConfig = new JSONObject();
                    jsonConfig.accumulateAll((Map) jobConfig.get(key));
                    final JobProperty newProperty = descriptor.newInstance(Stapler.getCurrentRequest(), jsonConfig);
                    if (newProperty != null) {
                        final JobProperty oldProperty = job.getProperty(newProperty.getClass());
                        job.removeProperty(oldProperty);
                        job.addProperty(newProperty);
                    }
                }

            }
//            Map<String, Descriptor<?>> jobWrappers = getDescriptors(BuildWrapper.class);
//
//
////            for(BuildWrapper wrapper :  job.getBuildWrappersList()){
////                Descriptor<BuildWrapper> descriptor = wrapper.getDescriptor();
//////                descriptor.
//////            descriptor.newInstance()
////
////            }
            job.save();

        } catch (final FileNotFoundException _) {
            //do nothing
        }

    }


    private <T extends Describable<T>, D extends Descriptor<T>> Map<String, Descriptor<?>> getDescriptors(final Class<T> c) {
        final Jenkins jenkins = Jenkins.getActiveInstance();
        final Map<String, Descriptor<?>> r = new HashMap<>();
        if (jenkins == null) {
            return r;
        }
        for (final Descriptor<?> d : jenkins.getDescriptorList(c)) {
            r.put(d.clazz.getSimpleName(), d);
        }
        return r;
    }
}
