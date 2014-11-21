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

import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import hudson.ExtensionList;
import hudson.Launcher;
import hudson.model.BuildListener;
import java.util.Arrays;
import java.util.List;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DotCiExtensionsHelperTest {

    @Test
    public void should_create_plugins(){
        Jenkins jenkins = mock(Jenkins.class);
        DotCiPluginAdapter plugin1 = getPlugin();
        ExtensionList extensionList = mock(ExtensionList.class);
        when(extensionList.iterator()).thenReturn(Arrays.asList(plugin1).iterator());
        when(jenkins.getExtensionList(any(Class.class))).thenReturn(extensionList);
        List<DotCiPluginAdapter> plugins = new DotCiExtensionsHelper(jenkins).createPlugins(Arrays.asList("plugin1"));

        Assert.assertNotNull(plugins);
        Assert.assertEquals(plugin1.getName(),plugins.get(0).getName());

    }

    private DotCiPluginAdapter getPlugin() {
        return new FakePlugin() ;
    }

    private static class FakePlugin extends DotCiPluginAdapter{
        public FakePlugin(){
            super("plugin1",null);
        }


        @Override
        public boolean perform(DynamicBuild dynamicBuild, Launcher launcher, BuildListener listener) {
            return false;
        }
    }


}