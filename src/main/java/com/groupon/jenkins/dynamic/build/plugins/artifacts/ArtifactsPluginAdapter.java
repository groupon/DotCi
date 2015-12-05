package com.groupon.jenkins.dynamic.build.plugins.artifacts;

import com.groupon.jenkins.buildtype.plugins.*;
import com.groupon.jenkins.dynamic.build.*;
import hudson.*;
import hudson.model.*;
import hudson.tasks.*;

import java.io.*;

@Extension
public class ArtifactsPluginAdapter extends DotCiPluginAdapter {

    public ArtifactsPluginAdapter() {
        super("artifacts");
    }

    @Override
    public String getPluginInputFiles() {
        return (String) options;
    }

    @Override
    public boolean perform(DynamicBuild dynamicBuild, Launcher launcher, BuildListener listener) {
        ArtifactArchiver archiver = new ArtifactArchiver((String) options, null, true);
        try {
            return archiver.perform((AbstractBuild) dynamicBuild, launcher, listener);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace(listener.getLogger());
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace(listener.getLogger());
        }
        return false;
    }

    @Override
    public void runFinished(DynamicSubBuild run, DynamicBuild parent, BuildListener listener) throws IOException {
        copyFiles(run, parent, (String) options, listener);
    }
}
