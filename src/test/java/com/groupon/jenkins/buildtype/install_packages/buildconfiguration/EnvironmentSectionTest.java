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

import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.MapValue;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import static com.groupon.jenkins.testhelpers.TestHelpers.configMap;
import static com.groupon.jenkins.testhelpers.TestHelpers.map;

public class EnvironmentSectionTest {

    private ShellCommands commands;

    @Before
    public void setUp() {
        MapValue<String, Object> config = configMap("image", "surya/cool_docker_image");
        EnvironmentSection envSection = new EnvironmentSection(config);
        this.commands = envSection.getDockerBuildRunScriptForImage("buildId", map("meow", "purr"));
    }

    @Test
    public void should_pull_docker_container_before_building() {
        assertEquals("docker pull  surya/cool_docker_image", commands.get(0));
    }

    @Test
    public void should_export_env_vars_to_running_container() {
        assertTrue(commands.get(1).contains("-e \"meow=purr\""));
    }

    @Test
    public void should_start_and_link_services_if_services_are_specified() {
        MapValue<String, Object> config = configMap("image", "surya/cool_docker_image", "services", "mongodb");
        EnvironmentSection envSection = new EnvironmentSection(config);
        this.commands = envSection.getDockerBuildRunScriptForImage("buildId", map("meow", "purr"));
        assertEquals("docker pull  mongodb", commands.get(1));
        assertEquals("docker run -d --name mongodb_buildId mongodb", commands.get(2));
        assertTrue(commands.get(3).contains("-link mongodb_buildId:mongodb"));
    }

    @Test
    public void should_inject_default_socat_if_services_are_specified() {
        MapValue<String, Object> config = configMap("image", "surya/cool_docker_image", "services", "mysql");
        EnvironmentSection envSection = new EnvironmentSection(config);
        this.commands = envSection.getDockerBuildRunScriptForLocal("buildId", map("meow", "purr"), "sh -c \"env && echo meooooow");
        assertEquals("docker pull  mysql", commands.get(1));
        assertTrue(commands.get(3).contains("-x /usr/bin/socat"));
        assertTrue(commands.get(3).contains("meooooow"));
    }
}
