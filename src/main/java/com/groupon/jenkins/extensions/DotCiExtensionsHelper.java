/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.groupon.jenkins.extensions;

import com.google.common.collect.Iterables;
import com.groupon.jenkins.buildtype.InvalidBuildConfigurationException;
import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DotCiExtensionsHelper {
    private Jenkins jenkins;

    DotCiExtensionsHelper(Jenkins jenkins) {
        this.jenkins = jenkins;
    }

    public DotCiExtensionsHelper() {
        this(Jenkins.getInstance());
    }

    public List<DotCiPluginAdapter> createPlugins(List<?> pluginSpecs) {
        return createPlugins(pluginSpecs, DotCiPluginAdapter.class);
    }

    public List<PostBuildNotifier> createNotifiers(List<?> notifierSpecs) {
        return createPlugins(notifierSpecs, PostBuildNotifier.class);
    }

    private <T extends DotCiExtension> List<T> createPlugins(List<?> pluginSpecs, Class<T> extensionClass) {
        if (Iterables.isEmpty(pluginSpecs)) {
            return Collections.emptyList();
        }
        List<T> plugins = new ArrayList<T>(pluginSpecs.size());
        for (Object pluginSpec : pluginSpecs) {
            String pluginName;
            Object options;
            if (pluginSpec instanceof String) {
                pluginName = (String) pluginSpec;
                options = new HashMap<Object, Object>();
            } else { // has to be a Map
                Map<String, Object> pluginSpecMap = (Map<String, Object>) pluginSpec;
                pluginName = Iterables.getOnlyElement(pluginSpecMap.keySet());
                options = pluginSpecMap.get(pluginName);
            }
            plugins.add(create(pluginName, options, extensionClass));
        }
        return plugins;
    }


    public <T extends DotCiExtension> T create(String pluginName, Object options, Class<T> extensionClass) {
        for (T adapter : all(extensionClass)) {
            if (adapter.getName().equals(pluginName)) {
                try {
                    adapter = (T) adapter.getClass().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                adapter.setOptions(options);
                return adapter;
            }

        }

        for (Descriptor<?> pluginDescriptor : getDescriptors()) {
            if (pluginDescriptor.clazz.getSimpleName().equals(pluginName)) {
                return (T) new GenericSimpleBuildStepPlugin(pluginDescriptor, options);
            }
        }
        throw new InvalidBuildConfigurationException("Plugin " + pluginName + " not supported");
    }

    private ArrayList<Descriptor<?>> getDescriptors() {
        ArrayList<Descriptor<?>> r = new ArrayList<Descriptor<?>>();
        r.addAll(getClassList(Builder.class));
        r.addAll(getClassList(Publisher.class));
        return r;
    }

    private <T extends Describable<T>, D extends Descriptor<T>> List<Descriptor<?>> getClassList(Class<T> c) {
        ArrayList<Descriptor<?>> r = new ArrayList<Descriptor<?>>();
        if (jenkins == null) {
            return new ArrayList<Descriptor<?>>();
        }
        for (Descriptor<?> d : jenkins.getDescriptorList(c)) {
            if (SimpleBuildStep.class.isAssignableFrom(d.clazz)) {
                r.add(d);
            }
        }
        return r;
    }

    public <T extends ExtensionPoint> Iterable<T> all(Class<T> extensionClass) {
        return jenkins.getExtensionList(extensionClass);
    }
}
