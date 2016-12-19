/*
The MIT License (MIT)

Copyright (c) 2016, Groupon, Inc.

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
package com.groupon.jenkins.buildtype.dockercompose.buildconfiguration;

import com.groupon.jenkins.buildtype.InvalidBuildConfigurationException;

import java.util.ArrayList;
import java.util.List;

public class GenericCommandsSection {
    public static final String INVALID_CI_YML_COMMANDS_SHOULD_BE_STRING_OR_LIST = "Invalid .ci.yml. Commands should be a list of string or a single string.";
    private final Object commandsConfig;

    public GenericCommandsSection(final Object commandSectionConfig) {

        if (commandSectionConfig != null && !(commandSectionConfig instanceof String) && !(commandSectionConfig instanceof List)) {
            throw new InvalidBuildConfigurationException(INVALID_CI_YML_COMMANDS_SHOULD_BE_STRING_OR_LIST);
        }

        this.commandsConfig = commandSectionConfig;

    }

    public List<String> getCommands() {
        if (this.commandsConfig == null) return new ArrayList<>();

        final List<String> commands = new ArrayList<>();
        if (this.commandsConfig instanceof String) {
            commands.add((String) this.commandsConfig);
        } else if (this.commandsConfig instanceof List) {
            final List l = (List) this.commandsConfig;

            for (final Object v : l) {
                if (!(v instanceof String)) {
                    throw new RuntimeException(String.format("Unexpected type: %s. Expected String .", v.getClass().getName()));
                }
                commands.add((String) v);
            }
        }
        return commands;
    }
}
