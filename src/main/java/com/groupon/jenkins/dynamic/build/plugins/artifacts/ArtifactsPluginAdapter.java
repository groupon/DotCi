package com.groupon.jenkins.dynamic.build.plugins.artifacts;

import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.DynamicSubBuild;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.ArtifactArchiver;

import java.io.IOException;

@Extension
public class ArtifactsPluginAdapter extends DotCiPluginAdapter {

    public ArtifactsPluginAdapter() {
        super("artifacts");
    }

    @Override
    public String getPluginInputFiles() {
        return (String) this.options;
    }

    @Override
    public boolean perform(final DynamicBuild dynamicBuild, final Launcher launcher, final BuildListener listener) {
        final ArtifactArchiver archiver = new ArtifactArchiver((String) this.options, null, true);
        try {
            return archiver.perform((AbstractBuild) dynamicBuild, launcher, listener);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace(listener.getLogger());
        } catch (final IOException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace(listener.getLogger());
        }
        return false;
    }

    @Override
    public void runFinished(final DynamicSubBuild run, final DynamicBuild parent, final BuildListener listener) throws IOException {
        copyFiles(run, parent, (String) this.options, listener);
    }
}
