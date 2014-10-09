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
package com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

import static com.groupon.jenkins.testhelpers.TestHelpers.map;

public class ListOrMapOrStringTest {

    @Test(expected = UnsupportedOperationException.class)
    public void shouldnt_support_appending_values() {
        new ListOrMapOrString(null).append(new ListOrMapOrString(null));
    }

    @Test
    public void should_return_value_form_map_converted_into_list() {
        ListOrMapOrString configValue = new ListOrMapOrString(map("unit", "blah", "intergration", "spec intergration"));
        List<String> keyValues = configValue.getValues("unit");
        assertEquals(1, keyValues.size());
        assertTrue(keyValues.contains("blah"));
    }

    @Test
    public void should_allow_list_values() {
        ListOrMapOrString configValue = new ListOrMapOrString(Arrays.asList("echo meow", "echo purr"));
        assertEquals("echo meow", configValue.getValuesList().get(0));
        assertEquals("echo purr", configValue.getValuesList().get(1));
    }

    @Test
    public void should_allow_single_string_value() {
        ListOrMapOrString configValue = new ListOrMapOrString("echo meow");
        assertEquals("echo meow", configValue.getValuesList().get(0));
    }

    @Test
    public void should_consider_map_with_single_key_value_as_single_value() {
        ListOrMapOrString configValue = new ListOrMapOrString(map("unit", "blah"));

        assertFalse(configValue.isMap());
        assertEquals("blah", configValue.getValuesList().iterator().next());
    }

    @Test
    public void should_return_requested_type(){

        ListOrMapOrString config = new ListOrMapOrString(map("unit", "blah", "integration","meow"));
        Map configValue = config.getValue(Map.class);
        assertEquals("blah",configValue.get("unit"));
    }
}
