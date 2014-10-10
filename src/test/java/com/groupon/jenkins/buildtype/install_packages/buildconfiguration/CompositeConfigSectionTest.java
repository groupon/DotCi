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

import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.MapValue;

import static com.google.common.collect.Iterables.get;
import static com.groupon.jenkins.testhelpers.TestHelpers.list;
import static com.groupon.jenkins.testhelpers.TestHelpers.map;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompositeConfigSectionTest {

    @Test
    public void yml_with_unrecognized_keys_is_invalid() {
        ConfigSection buildConfiguration = new BuildConfiguration(new MapValue(map("environment", map("not_legit_key", ""))));
        assertFalse(buildConfiguration.isValid());
    }

    @Test
    public void yml_with_invalid_value_format_is_invalid() {
        CompositeConfigSection buildConfiguration = new BuildConfiguration(new MapValue(map("environment", "")));
        assertFalse(buildConfiguration.isValid());
    }

    @Test
    public void yml_with_missing_keys_is_invalid() {
        ConfigSection buildConfiguration = new BuildConfiguration(new MapValue(map("environment", map("language", "ruby"))));
        assertTrue(buildConfiguration.isValid());
    }

    @Test
    public void should_specify_unrecognized_key_in_error_message() {
        CompositeConfigSection buildConfiguration = new BuildConfiguration(new MapValue(map("environment", "", "unknown_key", "")));
        assertEquals("Unrecognized key unknown_key in .ci.yml", get(buildConfiguration.getValidationErrors(), 0));
    }

    @Test
    public void should_check_sub_sections_for_errors_too() {
        CompositeConfigSection buildConfiguration = new BuildConfiguration(new MapValue(map("environment", "")));
        ConfigSection validConfigSection = mock(ConfigSection.class);
        ConfigSection inValidConfigSection = mock(ConfigSection.class);
        when(inValidConfigSection.getValidationErrors()).thenReturn(list("error_message"));
        when(inValidConfigSection.getName()).thenReturn("environment");
        when(validConfigSection.getValidationErrors()).thenReturn(Lists.<String> newArrayList());
        buildConfiguration.setSubSections(validConfigSection, inValidConfigSection);
        assertFalse(buildConfiguration.isValid());
        assertThat(buildConfiguration.getValidationErrors(), Matchers.<String> iterableWithSize(1));
        assertThat(buildConfiguration.getValidationErrors(), Matchers.<String> iterableWithSize(1));
        assertThat(buildConfiguration.getValidationErrors(), contains("error_message"));
    }
}
