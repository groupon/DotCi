package com.groupon.jenkins;

import hudson.Extension;
import hudson.Plugin;
import jenkins.model.Jenkins;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

@Extension
public class DotCiPlugin extends Plugin{
    private static final Logger LOGGER = Logger.getLogger(DotCiPlugin.class.getName());
    @Override
    public void start() throws Exception {
        File configsDir = new File(Jenkins.getInstance().getRootDir(), "pluginConfigs");
        File dotciPluginYml = new File(configsDir, "dotci.yml");

        if (dotciPluginYml.exists()) {
            LOGGER.info("Configuring dotci plugin from " + dotciPluginYml.getAbsolutePath());
            try (InputStream in = new BufferedInputStream(new FileInputStream(dotciPluginYml))) {
                Constructor constructor = new Constructor(SetupConfig.class);
                Yaml yaml = new Yaml(constructor);
                SetupConfig configuration = (SetupConfig) yaml.load(in);
                configuration.save();
            }
        } else {
            LOGGER.info(dotciPluginYml.getAbsolutePath() + " file not found.");
        }
    }
}
