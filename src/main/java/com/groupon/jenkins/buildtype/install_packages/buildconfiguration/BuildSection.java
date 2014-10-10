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

import java.util.LinkedList;
import java.util.List;

import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.BooleanValue;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListOrMapOrString;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListOrSingleValue;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListValue;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.MapValue;

public class BuildSection extends CompositeConfigSection {

    public static final String NAME = "build";
    private final BuildRunSection buildRunSection;
    private final AfterRunSection afterRunSection;
    private final CheckoutSection checkoutSection;
    private final BeforeSection beforeSection;
    private final SkipSection skipSection;

    public BuildSection(MapValue<String, ?> configValue) {
        super(NAME, configValue);
        this.skipSection = new SkipSection(getSectionConfig(SkipSection.NAME, BooleanValue.class));
        this.checkoutSection = new CheckoutSection(getSectionConfig(CheckoutSection.NAME, ListValue.class));
        this.beforeSection = new BeforeSection(getSectionConfig(BeforeSection.NAME, ListOrSingleValue.class));
        this.buildRunSection = new BuildRunSection(getSectionConfig(BuildRunSection.NAME, ListOrMapOrString.class));
        this.afterRunSection = new AfterRunSection(getSectionConfig("after", ListOrSingleValue.class));
        setSubSections(skipSection, checkoutSection, new BeforeInstallSection(getSectionConfig(BeforeInstallSection.NAME, ListOrSingleValue.class)), new SerialExecutionSection("info", getSectionConfig("info", ListOrSingleValue.class)), beforeSection, buildRunSection, afterRunSection);
    }

    public boolean isMultiScript() {
        return buildRunSection.isMultiConfig() || !isAfterRunEmpty();
    }

    private boolean isAfterRunEmpty() {
        return afterRunSection.getConfigValue().isEmpty();
    }

    public List<String> getScriptKeys() {
        List<String> keys = new LinkedList<String>();
        if (buildRunSection.isMultiConfig()) {
            keys.addAll(buildRunSection.getKeys());
        } else {
            keys.add("default");
        }
        if (!isAfterRunEmpty()) {
            keys.add("post_build");
        }
        return keys;
    }

    public boolean isSkipped() {
        return skipSection.isSkipped();
    }

}
