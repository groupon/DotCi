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

package com.groupon.jenkins.buildtype.dockercompose;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import hudson.matrix.Combination;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;

public class BuildConfigurationTest {
    @Test
    public void should_parallelize_on_run() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("run", of("unit", "command", "integration", "integration")));
        final Iterable<Combination> axisList = buildConfiguration.getAxisList().list();
        Assert.assertEquals(2, Iterables.size(axisList));
        Assert.assertEquals("script=unit", Iterables.get(axisList, 0).toString());
        Assert.assertEquals("script=integration", Iterables.get(axisList, 1).toString());
    }

    @Test
    public void should_cleanup_after_test_run() {
        final ShellCommands commands = getCleanupCommands();
        Assert.assertEquals("docker-compose -f docker-compose.yml down", commands.get(commands.size() - 1));
    }

    @Test
    public void should_pull_latest_image_from_registry() {
        final ShellCommands commands = getRunCommands();
        Assert.assertEquals("docker-compose -f docker-compose.yml pull", commands.get(5));
    }

    @Test
    public void should_run_cmd_from_ci_yml() {
        final ShellCommands commands = getRunCommands();
        Assert.assertEquals("export COMPOSE_CMD='docker-compose -f docker-compose.yml run -T unit command'", commands.get(6));
        Assert.assertEquals(" set +e && hash unbuffer >/dev/null 2>&1 ;  if [ $? = 0 ]; then set -e && unbuffer $COMPOSE_CMD ;else set -e && $COMPOSE_CMD ;fi", commands.get(7));
    }

    @Test
    public void should_run_before_each_command_string() {
        final ShellCommands commands = getRunCommands(ImmutableMap.of("before_each", "before_each cmd", "run", of("unit", "command", "integration", "integration")));
        Assert.assertEquals("before_each cmd", commands.get(5));
    }

    @Test
    public void should_run_before_each_command_list() {
        final ShellCommands commands = getRunCommands(ImmutableMap.of("before_each", ImmutableList.of("cmd 1", "cmd 2"), "run", of("unit", "command", "integration", "integration")));
        Assert.assertEquals("cmd 1", commands.get(5));
        Assert.assertEquals("cmd 2", commands.get(6));
    }

    @Test
    public void should_run_before_run_command_string() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("before_run", "before_run cmd", "run", of("unit", "command", "integration", "integration")));
        final ShellCommands commands = buildConfiguration.getBeforeRunCommandsIfPresent();
        Assert.assertEquals(1, commands.size());
        Assert.assertEquals("before_run cmd", commands.get(0));
    }

    @Test
    public void should_run_before_run_command_list() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("before_run", ImmutableList.of("cmd 1", "cmd 2"), "run", of("unit", "command", "integration", "integration")));
        final ShellCommands commands = buildConfiguration.getBeforeRunCommandsIfPresent();
        Assert.assertEquals(2, commands.size());
        Assert.assertEquals("cmd 1", commands.get(0));
        Assert.assertEquals("cmd 2", commands.get(1));
    }

    @Test
    public void should_not_run_before_run_for_parallel_subbuild() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("before_run", "before_run cmd", "run", of("unit", "command", "integration", "integration")));
        final ShellCommands commands = buildConfiguration.getCommands(Combination.fromString("script=unit"), getEnvVars()).get(0);
        for (final String command : commands.getCommands()) {
            Assert.assertNotEquals("before_run cmd", command);
        }
    }

    @Test
    public void should_accept_alternative_docker_compose_file() {
        final ShellCommands commands = getRunCommands(ImmutableMap.of("docker-compose-file", "./jenkins/docker-compose.yml", "run", of("unit", "command")));
        Assert.assertEquals("docker-compose -f ./jenkins/docker-compose.yml pull", commands.get(5));
        Assert.assertEquals("export COMPOSE_CMD='docker-compose -f ./jenkins/docker-compose.yml run -T unit command'", commands.get(6));
        Assert.assertEquals(" set +e && hash unbuffer >/dev/null 2>&1 ;  if [ $? = 0 ]; then set -e && unbuffer $COMPOSE_CMD ;else set -e && $COMPOSE_CMD ;fi", commands.get(7));
    }

    @Test
    public void should_be_skipped_if_skip_specified() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("skip", true));
        Assert.assertTrue(buildConfiguration.isSkipped());
    }

    @Test
    public void should_not_be_skipped_if_not_specified() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("before_run", "before_run cmd", "run", of("unit", "command", "integration", "integration")));
        Assert.assertFalse(buildConfiguration.isSkipped());
    }

    @Test
    public void should_run_after_each_string() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("run", of("unit", "command", "integration", "integration"), "after_each", "after_each cmd"));
        final ShellCommands commandList = buildConfiguration.getCommands(Combination.fromString("script=unit"), getEnvVars()).get(0);
        Assert.assertEquals("after_each cmd", Iterables.getLast(commandList.getCommands()));
    }

    @Test
    public void should_run_after_each_list() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("run", of("unit", "command", "integration", "integration"), "after_each", ImmutableList.of("cmd 1", "cmd 2")));
        final ShellCommands commandList = buildConfiguration.getCommands(Combination.fromString("script=unit"), getEnvVars()).get(0);
        Assert.assertEquals("cmd 1", commandList.get(commandList.size() - 2));
        Assert.assertEquals("cmd 2", commandList.get(commandList.size() - 1));
    }

    @Test
    public void should_not_run_after_run_after_parallized_builds_are_finished() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("run", of("unit", "command", "integration", "integration"), "after_run", "after_run cmd"));
        final ShellCommands commandList = buildConfiguration.getCommands(Combination.fromString("script=unit"), getEnvVars()).get(0);
        Assert.assertNotEquals("after_run cmd", Iterables.getLast(commandList.getCommands()));
    }

    @Test
    public void should_run_after_run_for_non_parallel() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("run", of("unit", "command"), "after_run", "after_run cmd"));
        final ShellCommands commandList = buildConfiguration.getCommands(Combination.fromString("script=unit"), getEnvVars()).get(0);
        Assert.assertEquals("after_run cmd", Iterables.getLast(commandList.getCommands()));
    }

    @Test
    public void should_run_after_run_command_string() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("after_run", "after_run cmd", "run", of("unit", "command", "integration", "integration")));
        final ShellCommands commands = buildConfiguration.getAfterRunCommandsIfPresent();
        Assert.assertEquals(1, commands.size());
        Assert.assertEquals("after_run cmd", commands.get(0));
    }

    @Test
    public void should_run_after_run_command_list() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("after_run", ImmutableList.of("cmd 1", "cmd 2"), "run", of("unit", "command", "integration", "integration")));
        final ShellCommands commands = buildConfiguration.getAfterRunCommandsIfPresent();
        Assert.assertEquals(2, commands.size());
        Assert.assertEquals("cmd 1", commands.get(0));
        Assert.assertEquals("cmd 2", commands.get(1));
    }

    private ShellCommands getRunCommands(final Map ciConfig) {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ciConfig);
        return buildConfiguration.getCommands(Combination.fromString("script=unit"), getEnvVars()).get(0);
    }

    private ShellCommands getRunCommands() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("run", of("unit", "command", "integration", "integration")));
        return buildConfiguration.getCommands(Combination.fromString("script=unit"), getEnvVars()).get(0);
    }

    private ShellCommands getCleanupCommands() {
        final BuildConfiguration buildConfiguration = new BuildConfiguration(ImmutableMap.of("run", of("unit", "command", "integration", "integration")));
        return buildConfiguration.getCommands(Combination.fromString("script=unit"), getEnvVars()).get(1);
    }

    private Map<String, Object> getEnvVars() {
        return ImmutableMap.<String, Object>of("DOTCI_DOCKER_COMPOSE_GIT_CLONE_URL", "git@github.com:groupon/DotCi.git", "DOTCI_BRANCH", "master", "SHA", "abc123");
    }
}
