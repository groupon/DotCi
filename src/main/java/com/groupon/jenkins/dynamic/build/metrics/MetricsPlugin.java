package com.groupon.jenkins.dynamic.build.metrics;

import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.IOException;

@Extension
public class MetricsPlugin extends DotCiPluginAdapter {
    public MetricsPlugin() {
        super("metrics","metrics.json");
    }

    @Override
    public boolean perform(DynamicBuild dynamicBuild, Launcher launcher, BuildListener listener) {
        try {
            FilePath[] metricFiles = dynamicBuild.getWorkspace().list("metrics.json");
            for(FilePath metric: metricFiles) {
                String metricsFile = metric.readToString();
                JSONArray metrics = JSONArray.fromObject(metricsFile);
                metrics.equals("");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
