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
package com.groupon.jenkins.notifications;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import hudson.model.BuildListener;
import org.junit.Test;

import static com.groupon.jenkins.testhelpers.DynamicBuildFactory.newBuild;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PostBuildNotifierTest {

    @Test
    public void should_notify_on_failure_if_failure_recovery() {
        PostBuildNotifierExt notifier = new PostBuildNotifierExt(PostBuildNotifier.Type.FAILURE_AND_RECOVERY);
        notifier.perform(newBuild().fail().get(), null);
        assertTrue(notifier.wasPerformed());

    }

    @Test
    public void should_notify_on_recovery_if_failure_recovery() {
        PostBuildNotifierExt notifier = new PostBuildNotifierExt(PostBuildNotifier.Type.FAILURE_AND_RECOVERY);
        notifier.perform(newBuild().success().recovery().get(), null);
        assertTrue(notifier.wasPerformed());
    }

    @Test
    public void should_not_notify_on_recovery_if_failure_recovery_and_previous_build_was_success() {
        PostBuildNotifierExt notifier = new PostBuildNotifierExt(PostBuildNotifier.Type.FAILURE_AND_RECOVERY);
        notifier.perform(newBuild().success().notRecovery().get(), null);
        assertFalse(notifier.wasPerformed());
    }

    @Test
    public void shouldt_notify_on_recovery_if_all_and_even_ifprevious_build_was_success() {
        PostBuildNotifierExt notifier = new PostBuildNotifierExt(PostBuildNotifier.Type.ALL);
        notifier.perform(newBuild().success().notRecovery().get(), null);
        assertTrue(notifier.wasPerformed());
    }

    private static class PostBuildNotifierExt extends PostBuildNotifier {

        private final Type type;
        private boolean wasPerformed;

        public PostBuildNotifierExt(Type type) {
            super("test");
            this.type = type;
        }

        @Override
        protected boolean notify(DynamicBuild build, BuildListener listener) {
            wasPerformed = true;
            return false;
        }

        public boolean wasPerformed() {
            return this.wasPerformed;
        }

        @Override
        protected Type getType() {
            return type;
        }

    }
}
