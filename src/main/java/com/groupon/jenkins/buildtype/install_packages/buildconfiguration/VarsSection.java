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
import hudson.matrix.Combination;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VarsSection extends ConfigSection<MapValue<String, String>> {
    public static final String NAME = "vars";

    public VarsSection(MapValue<String, String> configValue) {
        super(NAME, configValue, MergeStrategy.APPEND);
    }

    @Override
    public ShellCommands toScript(Combination combination) {
        return  new ShellCommands(getEnvVariablesExportCommands());
    }

    protected String[] getEnvVariablesExportCommands() {
        List<String> exportCommands = new LinkedList<String>();
        Map<String, String> configValue = getConfigValue().getValue();
        for (Map.Entry<String, String> var : configValue.entrySet()) {
            exportCommands.add(String.format("export %s=%s", var.getKey(), var.getValue()));
        }
        return exportCommands.toArray(new String[exportCommands.size()]);
    }

}
