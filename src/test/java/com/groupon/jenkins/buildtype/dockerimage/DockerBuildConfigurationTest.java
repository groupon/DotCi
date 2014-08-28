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

import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import hudson.matrix.Combination;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

import static com.google.common.collect.ImmutableMap.of;

public class DockerBuildConfigurationTest {

    @Test
    public void should_export_env_variables() throws Exception {
        DockerBuildConfiguration dockerBuildConfiguration = new DockerBuildConfiguration(of("image", "ubutu",
                "env", of("CI", "true"),
                "script", "echo success"),new ShellCommands());
        ShellCommands shellCommands = dockerBuildConfiguration.toShellCommands(null);
        Assert.assertEquals("docker run --rm --sig-proxy=true -e \"CI=true\" ubutu sh -c \"echo success\"",shellCommands.get(0));
    }

    @Test
    public void should_run_container_with_script_command() throws Exception {
        DockerBuildConfiguration dockerBuildConfiguration = new DockerBuildConfiguration(of("image", "ubutu",
                "script", Arrays.asList("echo step1", "echo step2")),new ShellCommands());
        ShellCommands shellCommands = dockerBuildConfiguration.toShellCommands(null);
        Assert.assertEquals("docker run --rm --sig-proxy=true ubutu sh -c \"echo step1 && echo step2\"",shellCommands.get(0));
    }


    @Test
    public void should_run_checkout_commands_before_script_commands() throws Exception {
        DockerBuildConfiguration dockerBuildConfiguration = new DockerBuildConfiguration(of("image", "ubutu",
                "script", Arrays.asList("echo step1", "echo step2")),new ShellCommands("checkout command1"));
        ShellCommands shellCommands = dockerBuildConfiguration.toShellCommands(null);
        Assert.assertEquals("docker run --rm --sig-proxy=true ubutu sh -c \"checkout command1 && echo step1 && echo step2\"",shellCommands.get(0));
    }

    @Test
    public void should_run_parallelized_if_script_is_parallized() throws Exception {
        DockerBuildConfiguration dockerBuildConfiguration = new DockerBuildConfiguration(of("image", "ubutu",
                "env", of("CI", "true"),
                "script", of("one","echo one", "two","echo two")),new ShellCommands());
        Assert.assertTrue(dockerBuildConfiguration.isParallized());
    }

    @Test
    public void should_run_parallel_build_of_the_combination_if_script_is_parallized() throws Exception {
        DockerBuildConfiguration dockerBuildConfiguration = new DockerBuildConfiguration(of("image", "ubutu",
                "script", of("one","echo one", "two","echo two")),new ShellCommands());
        Assert.assertTrue(dockerBuildConfiguration.isParallized());
        ShellCommands shellCommands = dockerBuildConfiguration.toShellCommands(new Combination(of("script","two")));
        Assert.assertEquals("docker run --rm --sig-proxy=true ubutu sh -c \"echo two\"",shellCommands.get(0));
    }

    @Test
    public void should_calculate_axis_list() throws Exception {
        DockerBuildConfiguration dockerBuildConfiguration = new DockerBuildConfiguration(of("image", "ubutu",
                "env", of("CI", "true"),
                "script", of("one","echo one", "two","echo two")),new ShellCommands());
        Assert.assertNotNull(dockerBuildConfiguration.getAxisList());
    }

}