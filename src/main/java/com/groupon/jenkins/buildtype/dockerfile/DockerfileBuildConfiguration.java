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

package com.groupon.jenkins.buildtype.dockerfile;

import com.groupon.jenkins.buildtype.docker.DockerBuildConfiguration;
import com.groupon.jenkins.buildtype.dockerimage.DockerCommandBuilder;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListOrMapOrString;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListValue;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.StringValue;
import com.groupon.jenkins.buildtype.util.config.Config;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import hudson.matrix.Combination;
import java.util.UUID;
import java.util.List;
import java.util.Map;

import static com.groupon.jenkins.buildtype.dockerimage.DockerCommandBuilder.dockerCommand;

public class DockerfileBuildConfiguration extends DockerBuildConfiguration {
    public DockerfileBuildConfiguration(Map config, String buildId, ShellCommands checkoutCommands) {
        super( new Config(config, "image", StringValue.class, "run_params", StringValue.class, "links", ListValue.class ,"command", ListOrMapOrString.class) ,
                buildId,
                checkoutCommands);
    }

    public ShellCommands toShellCommands(Combination combination) {
        ShellCommands shellCommands = new ShellCommands();
        DockerCommandBuilder dockerRunCommand = dockerCommand("run")
                .flag("rm")
                .flag("sig-proxy=true")
                .flag("v", "/usr/bin/docker:/bin/docker")
                .flag("v","/var/run/docker.sock:/docker.sock")
                .flag("e", "DOCKER_HOST=\"unix:///docker.sock\"")
                .args(getImageName(), "sh -cx \"" + getRunCommand(combination) + "\"");

        //shellCommands.addAll(linkServicesToRunCommand(dockerRunCommand, config.get("links", List.class)));

        //shellCommands.addAll(linkCleanupCommands);
        shellCommands.add(dockerRunCommand.get());

        return shellCommands;
    }

    @Override
    protected String getRunCommand(Combination combination) {
        String builtImageTagName = UUID.randomUUID().toString().replaceAll("-", "");
        String buildImageCommand = dockerCommand("build").flag("t", builtImageTag).args( ".").get();
        ShellCommands buildCommands = new ShellCommands();
        buildCommands.add(buildImageCommand);

        String buildCommand =  new ShellCommands(getCommandForCombination(combination)).toSingleShellCommand();
        String buildShellCommand = "sh -cx '" +  buildCommand+ "'";
        DockerCommandBuilder dockerRunCommand = dockerCommand("run")
                .flag("rm")
                .flag("sig-proxy=true")
                .bulkOptions(config.get("run_params", String.class))
                .args("dockerfile", buildShellCommand);
        buildCommands.addAll(linkServicesToRunCommand(dockerRunCommand, config.get("links", List.class)));

        String removeImageCommand = dockerCommand("rmi").args(builtImageTagName).get();
        buildCommands.add(removeImageCommand);

        return checkoutCommands.add(buildCommands).toSingleShellCommand();
    }
}
