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

public class ComposeFileNameSection {
    public static final String KEY = "docker-compose-file";
    protected static final String INVALID_CI_YML_COMPOSE_FILE_NAME = "Invalid .ci.yml. Compose file name should be a string.";
    private final String commposeFileName;

    public ComposeFileNameSection(final Object composeFileNameConfig) {
        if (composeFileNameConfig != null && !(composeFileNameConfig instanceof String)) {
            throw new InvalidBuildConfigurationException(INVALID_CI_YML_COMPOSE_FILE_NAME);
        }
        this.commposeFileName = (String) composeFileNameConfig;
    }

    public String getComposeFileName() {
        return this.commposeFileName != null ? this.commposeFileName : "docker-compose.yml";
    }
}
