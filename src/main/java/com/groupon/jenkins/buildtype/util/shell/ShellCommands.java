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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import org.apache.commons.lang.StringUtils;

public class ShellCommands {
    private final List<String> commands;
    public static final ShellCommands NOOP = new ShellCommands("NOOP", "");

    public ShellCommands(String... commands) {
        this.commands = Lists.newArrayList(commands);
    }

    public ShellCommands(List<String> commands) {
        this.commands = commands;
    }

    public String get(int index) {
        return commands.get(index);
    }

    public void add(String command) {
        commands.add(command);
    }

    protected String concat(String... lines) {
        return Joiner.on("\n").join(lines);
    }

    public String toShellScript() {
        return StringUtils.join(formatCommandsForConsole(), "\n");
    }

    private String[] formatCommandsForConsole() {
        List<String> formattedCommands = new ArrayList<String>(2 * commands.size());
        for (String command : commands) {
            formattedCommands.add(echoCommand(command));
            formattedCommands.add(command);
        }
        return formattedCommands.toArray(new String[formattedCommands.size()]);
    }

    private String echoCommand(String command) {
        return "echo $ ' " + escapeForShell(command) + "'";
    }

    public static final Escaper SHELL_ESCAPE;
    static {
        final Escapers.Builder builder = Escapers.builder();
        builder.addEscape('\'', "'\"'\"'");
        SHELL_ESCAPE = builder.build();
    }

    private String escapeForShell(String command) {
        return SHELL_ESCAPE.escape((String) command);
    }

    public Pattern regexp(String re) {
        int n = re.length() - 1, flags = Pattern.UNIX_LINES | Pattern.MULTILINE;
        if (re.charAt(0) != '^') {
            re = ".*" + re;
        }
        if (re.charAt(n) != '$') {
            re = re + ".*";
        }
        return Pattern.compile(re, flags);
    }

    public ShellCommands addAll(Stack<String> commands) {
        while (!commands.empty()){
           this.commands.add(commands.pop()) ;
        }
        return this;
    }

    public ShellCommands addAll(List<String> commands) {
        this.commands.addAll(commands);
        return this;
    }

    public ShellCommands add(ShellCommands otherCommands) {
        commands.addAll(otherCommands.getCommands());
        return this;
    }

    private Collection<? extends String> getCommands() {
        return commands;
    }

    public String toSingleShellCommand() {
        return Joiner.on(" && ").join(commands);
    }

    public static ShellCommands combine(List<ShellCommands> subPhases) {
        ShellCommands combinedShellCommands = new ShellCommands(new LinkedList<String>());
        for (ShellCommands shellCommands : subPhases) {
            if (shellCommands != null && ShellCommands.NOOP != shellCommands) {
                combinedShellCommands.add(shellCommands);
            }
        }
        return combinedShellCommands;
    }
}
