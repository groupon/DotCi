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

import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.ConfigSection.MergeStrategy;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListOrSingleValue;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.MapValue;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import hudson.matrix.Combination;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigSectionTest {
    @Test
    public void should_merge_only_if_other_config_is_not_empty() {
        ConfigSection<ListOrSingleValue<String>> configSection = getConfigSection("meow", MergeStrategy.REPLACE);
        ConfigSection<ListOrSingleValue<String>> replacedWithSection = getConfigSection(null, MergeStrategy.REPLACE);

        configSection.merge(replacedWithSection);
        assertFalse(configSection.getConfigValue().isEmpty());
        assertEquals("meow", configSection.getConfigValue().getValues().get(0));
    }

    @Test
    public void should_replace_if_merge_strategy_replace() {
        ConfigSection<ListOrSingleValue<String>> configSection = getConfigSection("meow", MergeStrategy.REPLACE);
        ConfigSection<ListOrSingleValue<String>> replacedWithSection = getConfigSection("purr", MergeStrategy.REPLACE);

        configSection.merge(replacedWithSection);
        assertEquals("purr", configSection.getConfigValue().getValues().get(0));
    }

    @Test
    public void should_append_if_merge_strategy_append() {
        ConfigSection<ListOrSingleValue<String>> configSection = getConfigSection("meow", MergeStrategy.APPEND);
        ConfigSection<ListOrSingleValue<String>> replacedWithSection = getConfigSection("purr", MergeStrategy.REPLACE);

        configSection.merge(replacedWithSection);
        assertTrue(configSection.getConfigValue().getValues().contains("meow"));
        assertTrue(configSection.getConfigValue().getValues().contains("purr"));
    }

    @Test
    public void should_validate_configvalue_for_errors() {
        MapValue<String, Object> invalidMapValue = new MapValue<String, Object>("not_a_map");
        ConfigSection<MapValue<String, Object>> invalidConfigSection = new ConfigSection<MapValue<String, Object>>("", invalidMapValue, MergeStrategy.APPEND) {
            @Override
            public ShellCommands toScript(Combination combination) {
                return null;
            }
        };
        assertFalse(invalidConfigSection.isValid());
    }

    private ConfigSection<ListOrSingleValue<String>> getConfigSection(String configValue, MergeStrategy mergeStrategy) {
        return new ConfigSection<ListOrSingleValue<String>>("", new ListOrSingleValue<String>(configValue), mergeStrategy) {
            @Override
            public ShellCommands toScript(Combination combination) {
                return null;
            }
        };
    }
}
