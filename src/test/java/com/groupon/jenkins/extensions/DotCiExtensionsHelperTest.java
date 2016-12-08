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

import com.google.common.collect.Lists;
import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import hudson.DescriptorExtensionList;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DotCiExtensionsHelperTest {

    @Test
    public void should_create_plugins_from_dotci_extensions() {
        Jenkins jenkins = mock(Jenkins.class);
        DotCiPluginAdapter plugin1 = getPlugin();
        ExtensionList extensionList = mock(ExtensionList.class);
        when(extensionList.iterator()).thenReturn(Arrays.asList(plugin1).iterator());
        when(jenkins.getExtensionList(any(Class.class))).thenReturn(extensionList);
        List<DotCiPluginAdapter> plugins = new DotCiExtensionsHelper(jenkins).createPlugins(Arrays.asList("plugin1"));

        Assert.assertNotNull(plugins);
        Assert.assertEquals(plugin1.getName(), plugins.get(0).getName());

    }

    @Test
    public void should_create_plugins_from_build_step_plugins() {
        Jenkins jenkins = mock(Jenkins.class);

        ExtensionList extensionList = mock(ExtensionList.class);
        when(extensionList.iterator()).thenReturn(Lists.newArrayList().iterator());
        when(jenkins.getExtensionList(any(Class.class))).thenReturn(extensionList);


        DescriptorExtensionList<Builder, Descriptor<Builder>> descriptors = mock(DescriptorExtensionList.class);
        when(jenkins.getDescriptorList(any(Class.class))).thenReturn(descriptors);
        Descriptor<Builder> buildStepDescriptor = new FakeBuildStepPlugin.FakeDescriptor();
        when(descriptors.iterator()).thenReturn(Lists.newArrayList(buildStepDescriptor).iterator());

        List<DotCiPluginAdapter> plugins = new DotCiExtensionsHelper(jenkins).createPlugins(Arrays.asList("FakeBuildStepPlugin"));

        Assert.assertNotNull(plugins);
        Assert.assertTrue(plugins.get(0) instanceof GenericSimpleBuildStepPlugin);


    }

    private DotCiPluginAdapter getPlugin() {
        return new FakePlugin();
    }

    private static class FakePlugin extends DotCiPluginAdapter {
        public FakePlugin() {
            super("plugin1", null);
        }

        @Override
        public boolean perform(DynamicBuild dynamicBuild, Launcher launcher, BuildListener listener) {
            return false;
        }
    }


    private static class FakeBuildStepPlugin extends Builder implements SimpleBuildStep {
        @Override
        public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {

        }


        private static class FakeDescriptor extends Descriptor {

            @Override
            public String getDisplayName() {
                return null;
            }
        }

    }

}
