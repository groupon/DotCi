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
import org.junit.Assert;
import org.junit.Test;

import static com.google.common.collect.ImmutableMap.of;

public class DockerBuildConfigurationTest {

    @Test
    public void should_export_env_variables() throws Exception {
        DockerBuildConfiguration dockerBuildConfiguration = new DockerBuildConfiguration(of("image", "ubutu",
                "env", of("CI", "true"),
                "script", "echo success"));
        ShellCommands shellCommands = dockerBuildConfiguration.toShellCommands();
        Assert.assertEquals("docker run --rm --sig-proxy=true -v `pwd`:/var/project -w /var/project -u `id -u` -e \"CI=true\" ubutu bash -c \"echo success\"",shellCommands.get(0));
    }

    @Test
    public void should_run_container_with_script_command() throws Exception {
        DockerBuildConfiguration dockerBuildConfiguration = new DockerBuildConfiguration(of("image", "ubutu",
                "script", "echo success"));
        ShellCommands shellCommands = dockerBuildConfiguration.toShellCommands();
        Assert.assertEquals("docker run --rm --sig-proxy=true -v `pwd`:/var/project -w /var/project -u `id -u` ubutu bash -c \"echo success\"",shellCommands.get(0));
    }

}