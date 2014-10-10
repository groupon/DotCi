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

import org.junit.Test;

import static com.groupon.jenkins.testhelpers.TestHelpers.configMap;
import static com.groupon.jenkins.testhelpers.TestHelpers.map;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BuildSectionTest {

    @Test
    public void should_be_multi_config_if_section_is_parallized() {
        BuildSection buildSection = new BuildSection(configMap("run", map("spec", "balh", "cuccumber", "cucke")));
        assertTrue(buildSection.isMultiScript());
    }

    @Test
    public void should_not_be_multi_config_if_section_is_not_parallized() {
        BuildSection buildSection = new BuildSection(configMap("run", map("spec", "balh")));
        assertFalse(buildSection.isMultiScript());
    }

    @Test
    public void should_be_multi_config_if_post_build_section_is_specified() {
        BuildSection buildSection = new BuildSection(configMap("run", map("spec", "balh"), "after", "deploy blah"));
        assertTrue(buildSection.isMultiScript());
    }

    @Test
    public void should_inculde_post_build_script_in_script_keys() {
        BuildSection buildSection = new BuildSection(configMap("run", map("spec", "balh"), "after", "deploy blah"));
        assertTrue(buildSection.getScriptKeys().contains("default"));
        assertTrue(buildSection.getScriptKeys().contains("post_build"));
    }

    @Test
    public void should_not_inculde_post_build_script_in_script_keys() {
        BuildSection buildSection = new BuildSection(configMap("run", map("spec", "balh", "cucumber", "asdfas")));
        assertTrue(buildSection.getScriptKeys().contains("spec"));
        assertFalse(buildSection.getScriptKeys().contains("post_build"));
    }

    @Test
    public void should_skip_build_if_skip_is_true() {
        BuildSection buildSection = new BuildSection(configMap("skip", true));
        assertTrue(buildSection.isSkipped());
    }

}
