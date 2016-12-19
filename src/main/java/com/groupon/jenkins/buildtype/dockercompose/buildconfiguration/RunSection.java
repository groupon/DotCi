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
import hudson.matrix.Axis;
import hudson.matrix.AxisList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public class RunSection {
    public static final String KEY = "run";
    protected static final String INVALID_CI_YML_REQUIRE_KEY_RUN_NOT_SPECIFIED = "Invalid .ci.yml. Required key run not specified.";
    protected static final String INVALID_CI_YML_RUN_NOT_A_MAP = "Invalid .ci.yml. 'run' needs to be a map.";
    private final Map config;

    public RunSection(final Object runConfig) {
        validate(runConfig);
        this.config = (Map) runConfig;
    }

    private void validate(final Object runConfig) {
        if (runConfig == null) {
            throw new InvalidBuildConfigurationException(INVALID_CI_YML_REQUIRE_KEY_RUN_NOT_SPECIFIED);
        }
        if (!(runConfig instanceof Map)) {
            throw new InvalidBuildConfigurationException(INVALID_CI_YML_RUN_NOT_A_MAP);
        }
    }

    public AxisList getAxisList() {
        final String dockerComposeContainerName = getOnlyRun();
        AxisList axisList = new AxisList(new Axis("script", dockerComposeContainerName));
        if (isParallelized()) {
            final Set commandKeys = this.config.keySet();
            axisList = new AxisList(new Axis("script", new ArrayList<>(commandKeys)));
        }
        return axisList;
    }

    public String getOnlyRun() {
        return (String) this.config.keySet().iterator().next();
    }

    public boolean isParallelized() {
        return this.config.size() > 1;
    }

    public List<String> getCommands(final String dockerComposeContainerName, final String fileName) {
        final List<String> commands = new ArrayList<>();
        final String dockerComposeRunCommand = getDockerComposeRunCommand(dockerComposeContainerName, fileName, this.config);
        commands.add(format("export COMPOSE_CMD='%s'", dockerComposeRunCommand));
        commands.add(" set +e && hash unbuffer >/dev/null 2>&1 ;  if [ $? = 0 ]; then set -e && unbuffer $COMPOSE_CMD ;else set -e && $COMPOSE_CMD ;fi");
        return commands;
    }

    private String getDockerComposeRunCommand(final String dockerComposeContainerName, final String fileName, final Map runConfig) {
        final Object dockerComposeCommand = runConfig.get(dockerComposeContainerName);
        if (dockerComposeCommand != null) {
            return String.format("docker-compose -f %s run -T %s %s", fileName, dockerComposeContainerName, dockerComposeCommand);
        } else {
            return String.format("docker-compose -f %s run %s ", fileName, dockerComposeContainerName);
        }
    }
}
