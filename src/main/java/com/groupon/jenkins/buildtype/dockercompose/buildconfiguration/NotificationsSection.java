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

import java.util.Collections;
import java.util.List;

public class NotificationsSection {
    public static final String KEY = "notifications";
    protected static final String INVALID_CI_YML_NOTIFIERS_SHOULD_BE_A_LIST = "Invalid .ci.yml. Notifications should be a list.";
    private final List<?> config;

    public NotificationsSection(final Object notifiersConfig) {
        if (notifiersConfig != null && !(notifiersConfig instanceof List)) {
            throw new InvalidBuildConfigurationException(INVALID_CI_YML_NOTIFIERS_SHOULD_BE_A_LIST);
        }

        this.config = (List<?>) notifiersConfig;
    }

    public List<?> getNotifiers() {
        return this.config != null ? this.config : Collections.emptyList();
    }
}
