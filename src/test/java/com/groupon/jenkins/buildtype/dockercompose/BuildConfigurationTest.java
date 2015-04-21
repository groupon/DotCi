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
import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import org.junit.Assert;
import static  com.google.common.collect.ImmutableMap.of;
import org.junit.Test;

import java.util.Map;

public class BuildConfigurationTest {
  @Test
  public  void should_parallelize_on_run(){
    BuildConfiguration buildConfiguration = new BuildConfiguration("groupon/DotCi", ImmutableMap.of("run", of("unit","command","integration","integration")), "buildId", new ShellCommands(), "abc123", 8);
    Iterable<Combination> axisList = buildConfiguration.getAxisList().list();
    Assert.assertEquals(2,Iterables.size(axisList));
    Assert.assertEquals("run=unit", Iterables.get(axisList,0).toString());
    Assert.assertEquals("run=integration", Iterables.get(axisList,1).toString());
  }
  @Test
  public  void should_cleanup_after_test_run(){
    ShellCommands commands = getRunCommands();
    Assert.assertEquals("trap \"docker-compose -p unitgroupondotci8 kill; docker-compose -p unitgroupondotci8 rm --force; exit\" PIPE QUIT INT HUP EXIT TERM",commands.get(0));
  }
  @Test
  public  void should_pull_latest_image_from_registry(){
    ShellCommands commands = getRunCommands();
    Assert.assertEquals("docker-compose -p unitgroupondotci8 pull",commands.get(1));
  }

  @Test
  public  void should_rebuild_dockerfile(){
    ShellCommands commands = getRunCommands();
    Assert.assertEquals("docker-compose -p unitgroupondotci8 build",commands.get(2));
  }

  @Test
  public  void should_run_cmd_from_ci_yml(){
    ShellCommands commands = getRunCommands();
    Assert.assertEquals("docker-compose -p unitgroupondotci8 run -T unit sh -xc 'command'",commands.get(3));
  }


  private ShellCommands getRunCommands() {
    BuildConfiguration buildConfiguration = new BuildConfiguration("groupon/DotCi", ImmutableMap.of("run", of("unit", "command", "integration", "integration")), "buildId", new ShellCommands(), "abc123", 8);
    return buildConfiguration.getCommands(Combination.fromString("run=unit"));
  }


}
