/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

P ermission is hereby granted, free of charge, to any person obtaining a copy
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

import com.groupon.jenkins.buildtype.dockerimage.DockerCommandBuilder;
import org.junit.Assert;
import org.junit.Test;

public class DockerComandBuilderTest {
    @Test
    public void should_use_single_dash_for_single_letter_option(){
        String dockerCommand = new DockerCommandBuilder("run").flag("d").args("java").get();
        Assert.assertEquals("docker run -d java",dockerCommand);
    }

    @Test
    public void should_use_double_dash_for_multiple_letter_option(){
        String dockerCommand = new DockerCommandBuilder("run").flag("rm").args("java").get();
        Assert.assertEquals("docker run --rm java",dockerCommand);
        dockerCommand = new DockerCommandBuilder("run").flag("name","meow").args("java").get();
        Assert.assertEquals("docker run --name meow java",dockerCommand);
    }
}
