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

import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListValue;
import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import com.groupon.jenkins.extensions.DotCiExtensionsHelper;
import hudson.matrix.Combination;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginsSection extends ConfigSection<ListValue<?>> {

    public static final String NAME = "plugins";

    public PluginsSection(ListValue<?> configValue) {
        super(NAME, configValue, MergeStrategy.APPEND);
    }

    public List<DotCiPluginAdapter> getPlugins() {
        if (getConfigValue().isEmpty()) {
            return Collections.emptyList();
        }
        return new DotCiExtensionsHelper().createPlugins(getConfigValue().getValue());
    }


    @Override
    public ShellCommands toScript(Combination combination) {
        return ShellCommands.NOOP;
    }

    @Override
    public Iterable<String> getValidationErrors() {
        List<String> validationErrors = new ArrayList<String>();
        for (DotCiPluginAdapter plugin: getPlugins()){
            validationErrors.addAll(plugin.getValidationErrors());
        }
        return validationErrors;
    }
}
