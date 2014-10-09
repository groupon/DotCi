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

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

import static com.groupon.jenkins.testhelpers.TestHelpers.configListOrSingleValue;
import static org.junit.Assert.assertEquals;

public class ServicesSectionTest {
    private List<String> startCommands;
    private List<String> cleanupCommands;
    private Iterable<String> linkCommands;

    @Before
    public void setupSection() {
        ServicesSection section = new ServicesSection(configListOrSingleValue("chairman/mongodb"));
        this.startCommands = section.getServiceStartCommands("buildId");
        this.cleanupCommands = section.getCleanupCommands("buildId");
        this.linkCommands = section.getContainerLinkCommands("buildId");
    }

    @Test
    public void should_pull_down_images() {
        assertEquals("docker pull  chairman/mongodb", startCommands.get(0));
    }

    @Test
    public void should_start_containers_in_deamon_mode() {
        assertEquals("docker run -d --name chairman_mongodb_buildId chairman/mongodb", startCommands.get(1));
    }

    @Test
    public void should_stop_containers_at_cleanup() {
        assertEquals("docker kill  chairman_mongodb_buildId", cleanupCommands.get(0));
    }

    @Test
    public void should_link_running_containers() {
        assertEquals("chairman_mongodb_buildId:mongodb", Iterables.get(linkCommands, 0));
    }

}
