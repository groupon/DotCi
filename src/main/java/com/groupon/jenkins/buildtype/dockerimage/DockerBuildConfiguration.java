/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.groupon.jenkins.buildtype.dockerimage;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListOrMapOrString;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListValue;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.StringValue;
import com.groupon.jenkins.buildtype.util.config.Config;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.groupon.jenkins.buildtype.dockerimage.DockerCommandBuilder.dockerCommand;

public class DockerBuildConfiguration {
    private Config config;
    private String buildId;
    private ShellCommands checkoutCommands;

    public DockerBuildConfiguration(Map config, String buildId, ShellCommands checkoutCommands) {
        this.buildId = buildId;
        this.checkoutCommands = checkoutCommands;
        this.config = new Config(config, "image", StringValue.class, "run_options", StringValue.class, "services", ListValue.class ,"script", ListOrMapOrString.class);
    }

    public ShellCommands toShellCommands(Combination combination) {
        ShellCommands shellCommands = new ShellCommands();
        startServices(shellCommands);

        DockerCommandBuilder dockerRunCommand = dockerCommand("run")
                .flag("rm")
                .flag("sig-proxy=true")
               .bulkOptions(config.get("run_options", String.class))
                .args(getImageName(), "sh -cx \"" + getRunCommand(combination) + "\"");


        linkServices(dockerRunCommand);

        shellCommands.add(dockerRunCommand.get());

        if(hasServices()) shellCommands.addAll(getCleanupCommands());

        return shellCommands;
    }

    private void linkServices(DockerCommandBuilder dockerRunCommand) {
        for (String link : getContainerLinkCommands()) {
            dockerRunCommand.flag("link", link);
        }
    }

    private void startServices(ShellCommands shellCommands) {
        if(hasServices()){
            List<Map> services = config.get("services", List.class);
            for (Map<String,String> service: services){
                String serviceImageName = service.get("image");
                String runCommand = DockerCommandBuilder.dockerCommand("run")
                        .flag("d")
                        .flag("name",getContainerId(serviceImageName))
                        .bulkOptions(service.get("run_options"))
                        .args(serviceImageName)
                        .get();
                shellCommands.add(runCommand);
            }
        }
    }

    boolean hasServices() {
        return config.containsKey("services");
    }

    protected String getContainerId(String serviceImageName) {
        String serviceId = serviceImageName.replaceAll("/", "_").replaceAll(":", "_").replaceAll("\\.", "_");
        return serviceId + "_" + buildId;
    }

    private String getRunCommand(Combination combination) {
        List commands;
        if(isParallized()){
            Map script = config.get("script", Map.class);
            Object scriptCommands= script.get(combination.get("script"));
            commands = scriptCommands instanceof List? (List)scriptCommands: Arrays.asList(scriptCommands);
        }else{
            commands = config.get("script", List.class);
        }

        return checkoutCommands.add(new ShellCommands(commands)).toSingleShellCommand();
    }

    private String getImageName() {
        return config.get("image",String.class);
    }



    public AxisList getAxisList() {
        AxisList  axisList = new AxisList(new Axis("script", "main"));
        if (isParallized()) {
            Set scriptKeys =  ((Map) config.get("script")).keySet();
            axisList = new AxisList(new Axis("script", new ArrayList<String>(scriptKeys)));
        }
        return axisList;
    }
    public Iterable<String> getContainerLinkCommands() {

        return hasServices()? Iterables.transform(getServiceImages(), new Function<String, String>() {
            @Override
            public String apply(String serviceImageName) {
                String serviceId = getServiceRuntimeId(serviceImageName);
                String runningImageId = getContainerId(serviceImageName);
                return runningImageId + ":" + serviceId;
            }
        }): new ArrayList<String>();

    }

    private String getServiceRuntimeId(String serviceImageName) {
        String[] serviceParts = serviceImageName.split("/");
        return serviceParts[serviceParts.length - 1].split(":")[0];
    }

    public List<String> getCleanupCommands() {
        List<String> cleanUpCommands = new ArrayList<String>();
        for (String serviceImageName : getServiceImages()) {
            String killCommand = dockerCommand("kill").args(getContainerId(serviceImageName)).get();
            String removeCommand = dockerCommand("rm").args(getContainerId(serviceImageName)).get();
            cleanUpCommands.add(killCommand);
            cleanUpCommands.add(removeCommand);
        }
        return cleanUpCommands;
    }

    private Iterable<String> getServiceImages() {
        List<Map<String,String>> services = config.get("services",List.class);
        return Iterables.transform(services,new Function<Map<String, String>, String>() {
            @Override
            public String apply(Map<String, String> service) {
                return service.get("image");
            }
        });
    }

    public boolean isParallized() {
        return config.get("script") instanceof Map;
    }
}
