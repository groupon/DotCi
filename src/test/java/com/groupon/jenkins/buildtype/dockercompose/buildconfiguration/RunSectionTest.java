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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RunSectionTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void run_config_should_be_specified() {
        this.expectedEx.expect(InvalidBuildConfigurationException.class);
        this.expectedEx.expectMessage(RunSection.INVALID_CI_YML_REQUIRE_KEY_RUN_NOT_SPECIFIED);
        new RunSection(null);
    }

    @Test
    public void run_config_should_be_a_map() {
        this.expectedEx.expect(InvalidBuildConfigurationException.class);
        this.expectedEx.expectMessage(RunSection.INVALID_CI_YML_RUN_NOT_A_MAP);
        new RunSection("not valid");
    }

}
