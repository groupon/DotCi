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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ConfigValue;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.MapValue;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import hudson.matrix.Combination;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.transform;

public class CompositeConfigSection extends ConfigSection<MapValue<String, ?>> {

    private ConfigSection[] subSections;

    protected CompositeConfigSection(String name, MapValue<String, ?> config) {
        super(name, config, MergeStrategy.APPEND);
    }

    protected void setSubSections(ConfigSection... subSections) {
        this.subSections = subSections;

    }

    @Override
    public void merge(ConfigSection otherConfigSection) {
        for (int i = 0; i < subSections.length; i++) {
            subSections[i].merge(((CompositeConfigSection) otherConfigSection).subSections[i]);
        }
    }

    @Override
    public ShellCommands toScript(Combination combination) {
        List<ShellCommands> subPhases = new ArrayList<ShellCommands>(subSections.length);
        for (int i = 0; i < subSections.length; i++) {
            subPhases.add(subSections[i].toScript(combination));
        }
        return ShellCommands.combine(subPhases);
    }

    private Iterable<String> validateForRedundantKeys(Iterable<String> allowedKeys) {
        if (getConfigValue().isEmpty()) {
            return Collections.emptyList();
        } else {
            ArrayList<String> specifiedKeys = new ArrayList<String>(getConfigValue().getValue().keySet());
            for (String allowedKey : allowedKeys) {
                specifiedKeys.remove(allowedKey);
            }
            return transform(specifiedKeys, new Function<String, String>() {
                @Override
                public String apply(String input) {
                    return String.format("Unrecognized key %s in %s", input, getName());
                }
            });
        }
    }

    @Override
    public Iterable<String> getValidationErrors() {
        Iterable<String> errors = super.getValidationErrors();
        if (Iterables.isEmpty(errors)) {
            List<String> allowedKeys = new ArrayList<String>(subSections.length);
            for (int i = 0; i < subSections.length; i++) {
                allowedKeys.add(subSections[i].getName());
            }
            errors = Iterables.concat(errors, validateForRedundantKeys(allowedKeys));
            for (int i = 0; i < subSections.length; i++) {
                errors = Iterables.concat(errors, subSections[i].getValidationErrors());
            }

        }
        return errors;
    }

    protected <T extends ConfigValue<?>> T getSectionConfig(String name, Class<T> configValueType) {
        return getConfigValue().getConfigValue(name, configValueType);
    }

    @Override
    protected Object getFinalConfigValue() {
        Map<String, Object> finalConfig = new HashMap<String, Object>(subSections.length);
        for (ConfigSection configSection : subSections) {
            finalConfig.put(configSection.getName(), configSection.getFinalConfigValue());
        }
        return finalConfig;
    }
}
