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

import hudson.matrix.Combination;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListOrSingleValue;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.StringValue;

import static org.junit.Assert.assertEquals;

public class PackagesSectionTest {

    @Test
    public void should_not_install_language_packages_if_language_is_unknown() {
        ListOrSingleValue<String> packageValue = new ListOrSingleValue<String>("package1");
        PackagesSection packagesSection = new PackagesSection(packageValue, new LanguageSection(new StringValue("unknown")), new LanguageVersionsSection(new ListOrSingleValue<String>("version1")));
        String installPackagesScript = packagesSection.getInstallPackagesScript(new Combination(ImmutableMap.of("one", "two")));
        assertEquals("install_packages package1", installPackagesScript);
    }
}
