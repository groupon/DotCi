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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import hudson.matrix.Combination;
import org.junit.Assert;
import static  com.google.common.collect.ImmutableMap.of;
import org.junit.Test;

import java.util.*;

public class BuildConfigurationTest {
  @Test
  public  void should_parallelize_on_run(){
    BuildConfiguration buildConfiguration = new BuildConfiguration("groupon/DotCi", ImmutableMap.of("run", of("unit","command","integration","integration")),  8);
    Iterable<Combination> axisList = buildConfiguration.getAxisList().list();
    Assert.assertEquals(2,Iterables.size(axisList));
    Assert.assertEquals("script=unit", Iterables.get(axisList,0).toString());
    Assert.assertEquals("script=integration", Iterables.get(axisList,1).toString());
  }

  @Test
  public  void should_cleanup_after_test_run(){
    ShellCommands commands = getRunCommands();
    Assert.assertEquals("trap \"docker-compose -f docker-compose.yml -p unitgroupondotci8 kill; docker-compose -f docker-compose.yml -p unitgroupondotci8 rm -v --force; exit\" PIPE QUIT INT HUP EXIT TERM",commands.get(6));
  }

  @Test
  public  void should_pull_latest_image_from_registry(){
    ShellCommands commands = getRunCommands();
    Assert.assertEquals("docker-compose -f docker-compose.yml -p unitgroupondotci8 pull",commands.get(7));
  }

  @Test
  public  void should_run_cmd_from_ci_yml(){
    ShellCommands commands = getRunCommands();
    Assert.assertEquals("docker-compose -f docker-compose.yml -p unitgroupondotci8 run -T unit command",commands.get(8));
  }

  @Test
  public void should_run_before_each_command_if_present(){
    ShellCommands commands = getRunCommands(ImmutableMap.of("before_each", "before_each cmd", "run", of("unit", "command", "integration", "integration")));
    Assert.assertEquals("sh -xc 'before_each cmd'", commands.get(6));
    Assert.assertEquals("trap \"docker-compose -f docker-compose.yml -p unitgroupondotci8 kill; docker-compose -f docker-compose.yml -p unitgroupondotci8 rm -v --force; exit\" PIPE QUIT INT HUP EXIT TERM",commands.get(7));
  }

  @Test
  public void should_run_before_run_command_in_before_run_if_present(){
    BuildConfiguration buildConfiguration = new BuildConfiguration("groupon/DotCi", ImmutableMap.of("before_run", "before_run cmd", "run", of("unit", "command")),  8);
    ShellCommands commands = buildConfiguration.getCommands(Combination.fromString("script=unit"), getEnvVars());
    Assert.assertEquals("sh -xc 'before_run cmd'", commands.get(6));
  }

  @Test
  public void should_run_before_run_with_checkout_command_with_non_parallel_build(){
    BuildConfiguration buildConfiguration = new BuildConfiguration("groupon/DotCi", ImmutableMap.of("before_run", "before_run cmd", "run", of("unit", "command", "integration", "integration")),  8);
    ShellCommands commands = buildConfiguration.getBeforeRunCommandWithCheckoutIfPresent(getEnvVars());
    Assert.assertEquals("chmod -R u+w . ; find . ! -path \"./deploykey_rsa.pub\" ! -path \"./deploykey_rsa\" -delete", commands.get(0));
    Assert.assertEquals("git init", commands.get(1));
    Assert.assertEquals("git remote add origin git@github.com:groupon/DotCi.git", commands.get(2));
    Assert.assertEquals("git fetch origin master", commands.get(3));
    Assert.assertEquals("git reset --hard  abc123", commands.get(4));
    Assert.assertEquals("sh -xc 'before_run cmd'", commands.get(6));
  }

  @Test
  public void should_not_run_before_run_for_parallel_subbuild(){
    BuildConfiguration buildConfiguration = new BuildConfiguration("groupon/DotCi", ImmutableMap.of("before_run", "before_run cmd", "run", of("unit", "command", "integration", "integration")),  8);
    ShellCommands commands = buildConfiguration.getCommands(Combination.fromString("script=unit"), getEnvVars());
    for (String command : commands.getCommands()) {
      Assert.assertNotEquals("before_run cmd", command);
    }
  }

  @Test
  public void should_accept_alternative_docker_compose_file(){
    ShellCommands commands = getRunCommands(ImmutableMap.of("docker-compose-file", "./jenkins/docker-compose.yml", "run",  of("unit", "command")));
    Assert.assertEquals("trap \"docker-compose -f ./jenkins/docker-compose.yml -p unitgroupondotci8 kill; docker-compose -f ./jenkins/docker-compose.yml -p unitgroupondotci8 rm -v --force; exit\" PIPE QUIT INT HUP EXIT TERM",commands.get(6));
    Assert.assertEquals("docker-compose -f ./jenkins/docker-compose.yml -p unitgroupondotci8 pull",commands.get(7));
    Assert.assertEquals("docker-compose -f ./jenkins/docker-compose.yml -p unitgroupondotci8 run -T unit command",commands.get(8));
  }

  private ShellCommands getRunCommands(Map ciConfig) {
    BuildConfiguration buildConfiguration = new BuildConfiguration("groupon/DotCi", ciConfig, 8);
    return buildConfiguration.getCommands(Combination.fromString("script=unit"), getEnvVars());
  }

  private ShellCommands getRunCommands() {
    BuildConfiguration buildConfiguration = new BuildConfiguration("groupon/DotCi", ImmutableMap.of("run", of("unit", "command", "integration", "integration")),  8);
    return buildConfiguration.getCommands(Combination.fromString("script=unit"), getEnvVars());
  }

  private Map<String, Object> getEnvVars() {
    return ImmutableMap.<String,Object>of("DOTCI_DOCKER_COMPOSE_GIT_CLONE_URL","git@github.com:groupon/DotCi.git", "DOTCI_BRANCH", "master", "SHA", "abc123");
  }
}
