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
import org.junit.Ignore;
import org.junit.Test;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;

public class DockerImageBuildConfigurationTest {


    @Test
    @Ignore
    public void should_support_nested_links(){
        DockerImageBuildConfiguration dockerImageBuildConfiguration = new DockerImageBuildConfiguration(of("image", "ubutu",
                "links",  asList(of("image","mysql",
                                     "links", asList(of("image","redis"))
                                 )),
                "command", "echo success"),"buildId", new ShellCommands());
        ShellCommands shellCommands = dockerImageBuildConfiguration.toShellCommands(null);
        Assert.assertEquals("docker run -d --name redis_buildId redis",shellCommands.get(0));
        Assert.assertEquals("docker run -d --name mysql_buildId --link redis_buildId:redis mysql",shellCommands.get(1));
        Assert.assertEquals("docker run --rm --sig-proxy=true --link mysql_buildId:mysql ubutu sh -cx \"echo success\"",shellCommands.get(2));
        Assert.assertEquals("docker kill  mysql_buildId",shellCommands.get(3));
        Assert.assertEquals("docker rm  mysql_buildId",shellCommands.get(4));
        Assert.assertEquals("docker kill  redis_buildId",shellCommands.get(5));
        Assert.assertEquals("docker rm  redis_buildId",shellCommands.get(6));

    }

    @Test
    public void should_start_and_link_containers_when_links_are_specified(){

        DockerImageBuildConfiguration dockerImageBuildConfiguration = new DockerImageBuildConfiguration(of("image", "ubutu",
                "links",  asList(of("image","mysql",
                                     "name", "custom_mysql_name",
                                    "command","meow")),
                "command", "echo success"),"buildId", new ShellCommands());
        ShellCommands shellCommands = dockerImageBuildConfiguration.toShellCommands(null);
        Assert.assertEquals("docker run -d --name mysql_buildId mysql sh -cx \"meow\"",shellCommands.get(0));
        Assert.assertEquals("docker run --rm --sig-proxy=true --link mysql_buildId:custom_mysql_name ubutu sh -cx \"echo success\"",shellCommands.get(1));
        Assert.assertEquals("docker kill  mysql_buildId",shellCommands.get(2));
    }

    @Test
    public void should_start_and_linked_containers_with_run_params_if_specified(){

        DockerImageBuildConfiguration dockerImageBuildConfiguration = new DockerImageBuildConfiguration(of("image", "ubutu",
                "links",  asList(of("image","mysql",
                                        "run_params","-v /var/test=/var/test"  )),
                "command", "echo success"),"buildId", new ShellCommands());
        ShellCommands shellCommands = dockerImageBuildConfiguration.toShellCommands(null);
        Assert.assertEquals("docker run -d --name mysql_buildId -v /var/test=/var/test mysql",shellCommands.get(0));
        Assert.assertEquals("docker run --rm --sig-proxy=true --link mysql_buildId:mysql ubutu sh -cx \"echo success\"",shellCommands.get(1));
        Assert.assertEquals("docker kill  mysql_buildId",shellCommands.get(2));
    }


    @Test
    public void should_run_container_with_specified_params() throws Exception {
        DockerImageBuildConfiguration dockerImageBuildConfiguration = new DockerImageBuildConfiguration(
                  of("image", "ubutu",
                     "run_params","-v /var/tmp=/var/tmp",
                     "command", "echo meow"),"buildId",new ShellCommands());
        ShellCommands shellCommands = dockerImageBuildConfiguration.toShellCommands(null);
        Assert.assertEquals("docker run --rm --sig-proxy=true -v /var/tmp=/var/tmp ubutu sh -cx \"echo meow\"",shellCommands.get(0));
    }


    @Test
    public void should_run_container_with_command() throws Exception {
        DockerImageBuildConfiguration dockerImageBuildConfiguration = new DockerImageBuildConfiguration(of("image", "ubutu",
                "command", Arrays.asList("echo step1", "echo step2")),"buildId",new ShellCommands());
        ShellCommands shellCommands = dockerImageBuildConfiguration.toShellCommands(null);
        Assert.assertEquals("docker run --rm --sig-proxy=true ubutu sh -cx \"echo step1 && echo step2\"",shellCommands.get(0));
    }


    @Test
    public void should_run_checkout_commands_before_commands() throws Exception {
        DockerImageBuildConfiguration dockerImageBuildConfiguration = new DockerImageBuildConfiguration(of("image", "ubutu",
                "command", Arrays.asList("echo step1", "echo step2")),"buildId",new ShellCommands("checkout command1"));
        ShellCommands shellCommands = dockerImageBuildConfiguration.toShellCommands(null);
        Assert.assertEquals("docker run --rm --sig-proxy=true ubutu sh -cx \"checkout command1 && echo step1 && echo step2\"",shellCommands.get(0));
    }

    @Test
    public void should_run_parallelized_if_command_is_parallized() throws Exception {
        DockerImageBuildConfiguration dockerImageBuildConfiguration = new DockerImageBuildConfiguration(of("image", "ubutu",
                "env", of("CI", "true"),
                "command", of("one","echo one", "two","echo two")),"buildId",new ShellCommands());
        Assert.assertTrue(dockerImageBuildConfiguration.isParallized());
    }

    @Test
    public void should_run_parallel_build_of_the_combination_if_command_is_parallized() throws Exception {
        DockerImageBuildConfiguration dockerImageBuildConfiguration = new DockerImageBuildConfiguration(of("image", "ubutu",
                "command", of("one","echo one", "two","echo two")),"buildId",new ShellCommands());
        Assert.assertTrue(dockerImageBuildConfiguration.isParallized());
        ShellCommands shellCommands = dockerImageBuildConfiguration.toShellCommands(new Combination(of("command","two")));
        Assert.assertEquals("docker run --rm --sig-proxy=true ubutu sh -cx \"echo two\"",shellCommands.get(0));
    }

    @Test
    public void should_calculate_axis_list() throws Exception {
        DockerImageBuildConfiguration dockerImageBuildConfiguration = new DockerImageBuildConfiguration(of("image", "ubutu",
                "env", of("CI", "true"),
                "command", of("one","echo one", "two","echo two")),"buildId", new ShellCommands());
        Assert.assertNotNull(dockerImageBuildConfiguration.getAxisList());
    }

}
