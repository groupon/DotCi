/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.groupon.jenkins.buildtype.install_packages.buildconfiguration;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListOrSingleValue;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import hudson.matrix.Combination;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import static com.groupon.jenkins.buildtype.dockerimage.DockerCommandBuilder.dockerCommand;

public class ServicesSection extends ConfigSection<ListOrSingleValue<String>> {

    public static final String NAME = "services";

    protected ServicesSection(ListOrSingleValue<String> configValue) {
        super(NAME, configValue, MergeStrategy.APPEND);
    }

    @Override
    public ShellCommands toScript(Combination combination) {
        return ShellCommands.NOOP;
    }

    public List<String> getServiceStartCommands(String buildId) {
        List<String> commands = new LinkedList<String>();
        for (String serviceImageName : getConfigValue().getValues()) {
            commands.add(dockerCommand("pull").args(serviceImageName).get());
            /* @formatter:off */
            String runCommand = dockerCommand("run")
            .flag("d")
            .flag("name",getContainerId(serviceImageName, buildId))
            .args(serviceImageName)
            .get();
           /* @formatter:on */
            commands.add(runCommand);
        }
        return commands;
    }

    protected String getContainerId(String serviceImageName, String buildId) {
        String serviceId = serviceImageName.replaceAll("/", "_").replaceAll(":", "_").replaceAll("\\.", "_");
        return serviceId + "_" + buildId;
    }

    public Iterable<String> getContainerLinkCommands(final String buildId) {
        return Iterables.transform(getConfigValue().getValues(), new Function<String, String>() {
            @Override
            public String apply(@Nullable String serviceImageName) {
                String serviceId = getServiceRuntimeId(serviceImageName);
                String runningImageId = getContainerId(serviceImageName, buildId);
                return runningImageId + ":" + serviceId;
            }
        });

    }

    private String getServiceRuntimeId(String serviceImageName) {
        String[] serviceParts = serviceImageName.split("/");
        return serviceParts[serviceParts.length - 1].split(":")[0];
    }

    public List<String> getCleanupCommands(String buildId) {
        List<String> commands = new LinkedList<String>();
        for (String serviceImageName : getConfigValue().getValues()) {
            String killCommand = dockerCommand("kill").args(getContainerId(serviceImageName, buildId)).get();
            String removeCommand = dockerCommand("rm").args(getContainerId(serviceImageName, buildId)).get();
            commands.add(killCommand);
            commands.add(removeCommand);
        }
        return commands;
    }
}
