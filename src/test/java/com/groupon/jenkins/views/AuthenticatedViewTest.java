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
package com.groupon.jenkins.views;

import com.groupon.jenkins.util.AuthenticationMixin;
import hudson.model.Descriptor.FormException;
import hudson.model.Item;
import hudson.model.ModifiableItemGroup;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AuthenticatedViewTest {

    @Spy
    AuthenticatedView myBuildsView = new AuthenticatedView("mybuilds") {

        @Override
        protected void submit(StaplerRequest req) throws IOException, ServletException, FormException {

        }

        @Override
        public void onJobRenamed(Item item, String oldName, String newName) {
        }
    };

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_create_item_iff_owner_is_modifiable() throws IOException, ServletException {
        ModifiableItemGroup<?> parentView = mock(ModifiableItemGroup.class);
        doReturn(parentView).when(myBuildsView).getOwnerItemGroup();
        myBuildsView.doCreateItem(null, null);
        verify(parentView).doCreateItem(null, null);
    }

    @Test
    public void should_automatically_log_user_in_when_accessed() throws IOException, ServletException {
        AuthenticationMixin authMixin = mock(AuthenticationMixin.class);
        doReturn(authMixin).when(myBuildsView).makeAuthenticationMixin();
        myBuildsView.getTarget();
        verify(authMixin).authenticate();
    }

}
