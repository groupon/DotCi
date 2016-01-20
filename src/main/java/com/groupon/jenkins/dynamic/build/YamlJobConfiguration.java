package com.groupon.jenkins.dynamic.build;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.listeners.ItemListener;
import hudson.tasks.BuildWrapper;
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

@Extension
public class YamlJobConfiguration extends ItemListener{

    public static final String CI_CONFIG_YML = ".ci.config.yml";

    @Override
    public void onCreated(Item item) {
        if (item instanceof DynamicProject) {
            try {
                apply((DynamicProject) item);
            } catch (IOException e) {
               throw  new RuntimeException(e) ;
            } catch (Descriptor.FormException e) {
               throw new RuntimeException(e);
            }
        }
    }


    public void apply(DynamicProject job) throws IOException, Descriptor.FormException {
        try{
            GHRepository repo = job.getGithubRepository();
            GHContent configFile = repo.getFileContent(CI_CONFIG_YML, repo.getMasterBranch());
            Yaml yaml= new Yaml();
            Map<String,Object> jobConfig= (Map) yaml.load(configFile.read());

            Map<String, Descriptor<?>> jobProperties = getDescriptors(JobProperty.class);

            for(String key: jobConfig.keySet()){
                    JobPropertyDescriptor descriptor = (JobPropertyDescriptor) (jobProperties.containsKey(key)? jobProperties.get(key): jobProperties.get(key+"Property"));
                    if(descriptor != null && descriptor.isApplicable(job.getClass())){
                        JSONObject jsonConfig=new JSONObject();
                        jsonConfig.accumulateAll((Map) jobConfig.get(key));
                        JobProperty newProperty  = descriptor.newInstance(Stapler.getCurrentRequest(),jsonConfig);
                        if(newProperty !=null){
                            JobProperty oldProperty = job.getProperty(newProperty.getClass());
                            job.removeProperty(oldProperty);
                            job.addProperty(newProperty );
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

        }catch (FileNotFoundException _){
           //do nothing
        }

    }


    private <T extends Describable<T>,D extends Descriptor<T>> Map<String,Descriptor<?>> getDescriptors(Class<T> c) {
        Jenkins jenkins = Jenkins.getActiveInstance();
        Map<String,Descriptor<?>> r = new HashMap<String, Descriptor<?>>();
        if (jenkins == null) {
            return r;
        }
        for (Descriptor<?> d : jenkins.getDescriptorList(c)) {
                r.put(d.clazz.getSimpleName(), d);
        }
        return r;
    }
}
