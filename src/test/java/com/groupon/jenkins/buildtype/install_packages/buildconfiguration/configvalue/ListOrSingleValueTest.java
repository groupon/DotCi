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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListOrSingleValueTest {

    @Test
    public void should_consider_single_string_as_list_of_one_value() {
        ListOrSingleValue<String> value = new ListOrSingleValue<String>("blah");
        assertEquals(1, value.size());
        assertEquals("blah", value.getValues().get(0));
    }

    @Test
    public void should_append_values_to_existing_list() {
        ListOrSingleValue<String> value = new ListOrSingleValue<String>("hello");
        ConfigValue<Object> appended = new ListOrSingleValue<String>("world");
        value.append(appended);
        assertEquals(2, value.size());
        assertEquals("hello", value.getValues().get(0));
        assertEquals("world", value.getValues().get(1));
    }

    @Test
    public void should_be_empty_if_not_specified() {
        ListOrSingleValue<String> value = new ListOrSingleValue<String>(null);
        assertTrue(value.isEmpty());
    }

    @Test
    public void should_be_empty_if_not_specified_or_list_is_empty() {
        ListOrSingleValue<String> value = new ListOrSingleValue<String>(Arrays.asList());
        assertTrue(value.isEmpty());
    }

    @Test
    public void should_treat_append_as_replace_if_value_is_empty() {
        ListOrSingleValue<String> value = new ListOrSingleValue<String>(null);
        ConfigValue<Object> appended = new ListOrSingleValue<String>("meow");
        value.append(appended);
        assertTrue(value.getValues().contains("meow"));
    }

    @Test
    public void should_ignore_append_if_value_being_replaced_is_empty() {
        ListOrSingleValue<String> value = new ListOrSingleValue<String>("meow");
        ConfigValue<Object> appended = new ListOrSingleValue<String>(null);
        value.append(appended);
        assertEquals(1, value.size());
        assertTrue(value.getValues().contains("meow"));
    }

}
