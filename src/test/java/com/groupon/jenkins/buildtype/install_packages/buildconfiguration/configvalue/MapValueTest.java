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

import org.junit.Test;

import static com.groupon.jenkins.testhelpers.TestHelpers.map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MapValueTest {

    @Test
    public void should_find_sub_value() {
        MapValue<String, String> mapValue = new MapValue<String, String>(map("skip", "true"));
        StringValue stringValue = mapValue.getConfigValue("skip", StringValue.class);
        assertEquals("true", stringValue.getValue());
    }

    @Test
    public void should_create_sub_value_with_null_value_if_curerent_value_is_null() {
        MapValue<String, String> mapValue = new MapValue<String, String>(null);
        StringValue stringValue = mapValue.getConfigValue("skip", StringValue.class);
        assertNotNull(stringValue);
    }

    @Test
    public void should_append_values_to_existing_values_on_append() {
        MapValue<String, String> mapValue = new MapValue<String, String>(map("one", "1"));
        MapValue<String, String> mapValueMergedIn = new MapValue<String, String>(map("two", "2"));
        mapValue.append(mapValueMergedIn);
        assertEquals("2", mapValue.getValue().get("two"));
    }
}
