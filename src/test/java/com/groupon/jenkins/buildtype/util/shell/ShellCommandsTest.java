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
package com.groupon.jenkins.buildtype.util.shell;

import org.junit.Assert;
import org.junit.Test;

public class ShellCommandsTest {
    @Test
    public void should_escape_quotes_in_echo() {
        final ShellCommands executionPhase = new ShellCommands("echo \"hello\"", "echo world");
        final String[] commands = executionPhase.toShellScript().split("\\r?\\n");
        Assert.assertEquals("echo $ \' echo \"hello\"\'", commands[0]);
    }

    @Test
    public void should_not_echo_commands_if_echo_turned_off() {
        final ShellCommands executionPhase = new ShellCommands(false, "echo meow");
        final String[] commands = executionPhase.toShellScript().split("\\r?\\n");
        Assert.assertEquals(1, commands.length);
        Assert.assertEquals("echo meow", commands[0]);
    }
}
