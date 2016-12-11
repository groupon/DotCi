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

package com.groupon.jenkins.buildtype.dockercompose;

import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import com.groupon.jenkins.extensions.DotCiExtensionsHelper;
import com.groupon.jenkins.git.GitUrl;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public class BuildConfiguration {

    private final Map config;

    public BuildConfiguration(final Map config) {
        this.config = config;
    }

    public static ShellCommands getCheckoutCommands(final Map<String, Object> dotCiEnvVars) {
        final String gitCloneUrl = (String) dotCiEnvVars.get("DOTCI_DOCKER_COMPOSE_GIT_CLONE_URL");
        final GitUrl gitRepoUrl = new GitUrl(gitCloneUrl);
        final boolean isPrivateRepo = Boolean.parseBoolean((String) dotCiEnvVars.get("DOTCI_IS_PRIVATE_REPO"));
        final String gitUrl = isPrivateRepo ? gitRepoUrl.getGitUrl() : gitCloneUrl;
        final ShellCommands shellCommands = new ShellCommands();
        shellCommands.add("chmod -R u+w . ; find . ! -path \"./deploykey_rsa.pub\" ! -path \"./deploykey_rsa\" -delete");
        shellCommands.add("git init");
        shellCommands.add(format("git remote add origin %s", gitUrl));

        if (dotCiEnvVars.get("DOTCI_PULL_REQUEST") != null) {
            if (isPrivateRepo) {

                shellCommands.add(format("ssh-agent bash -c \"ssh-add -D && ssh-add \\%s/deploykey_rsa && git fetch origin '+refs/pull/%s/merge:' \"", dotCiEnvVars.get("WORKSPACE"), dotCiEnvVars.get("DOTCI_PULL_REQUEST")));
            } else {
                shellCommands.add(format("git fetch origin \"+refs/pull/%s/merge:\"", dotCiEnvVars.get("DOTCI_PULL_REQUEST")));
            }
            shellCommands.add("git reset --hard FETCH_HEAD");
        } else {
            if (isPrivateRepo) {

                shellCommands.add(format("ssh-agent bash -c \"ssh-add -D && ssh-add \\%s/deploykey_rsa && git fetch origin %s \"", dotCiEnvVars.get("WORKSPACE"), dotCiEnvVars.get("DOTCI_BRANCH")));
            } else {
                shellCommands.add(format("git fetch origin %s", dotCiEnvVars.get("DOTCI_BRANCH")));
            }
            shellCommands.add(format("git reset --hard  %s", dotCiEnvVars.get("SHA")));
        }
        return shellCommands;
    }

    public ShellCommands getBeforeRunCommandsIfPresent() {
        return getShellCommands("before_run");
    }

    public ShellCommands getAfterRunCommandsIfPresent() {
        return getShellCommands("after_run");
    }

    public List<ShellCommands> getCommands(final Combination combination, final Map<String, Object> dotCiEnvVars) {
        final List<ShellCommands> allCommands = new ArrayList<>();
        final String dockerComposeContainerName = combination.get("script");
        final String projectName = (String) dotCiEnvVars.get("COMPOSE_PROJECT_NAME");
        final String fileName = getDockerComposeFileName();

        final ShellCommands shellCommands = new ShellCommands();
        shellCommands.add(BuildConfiguration.getCheckoutCommands(dotCiEnvVars));


        appendCommands("before", shellCommands); //deprecated
        appendCommands("before_each", shellCommands);

        shellCommands.add(String.format("docker-compose -f %s pull", fileName));
        if (this.config.get("run") != null) {
            final Map runConfig = (Map) this.config.get("run");
            final String dockerComposeRunCommand = getDockerComposeRunCommand(dockerComposeContainerName, fileName, runConfig);
            shellCommands.add(format("export COMPOSE_CMD='%s'", dockerComposeRunCommand));
            shellCommands.add(" set +e && hash unbuffer >/dev/null 2>&1 ;  if [ $? = 0 ]; then set -e && unbuffer $COMPOSE_CMD ;else set -e && $COMPOSE_CMD ;fi");
        }

        appendCommands("after_each", shellCommands);
        if (!isParallelized()) {
            appendCommands("after_run", shellCommands);
        }
        allCommands.add(shellCommands);
        allCommands.add(getCleanupCommands(dockerComposeContainerName, projectName));
        return allCommands;
    }

    private ShellCommands getCleanupCommands(final String dockerComposeContainerName, final String projectName) {
        final ShellCommands cleanupCommands = getCopyWorkDirIntoWorkspaceCommands(dockerComposeContainerName, projectName);
        cleanupCommands.add(String.format("docker-compose -f %s down", getDockerComposeFileName()));
        return cleanupCommands;
    }


    private String getDockerComposeRunCommand(final String dockerComposeContainerName, final String fileName, final Map runConfig) {
        final Object dockerComposeCommand = runConfig.get(dockerComposeContainerName);
        if (dockerComposeCommand != null) {
            return String.format("docker-compose -f %s run -T %s %s", fileName, dockerComposeContainerName, dockerComposeCommand);
        } else {
            return String.format("docker-compose -f %s run %s ", fileName, dockerComposeContainerName);
        }
    }

    public AxisList getAxisList() {
        final String dockerComposeContainerName = getOnlyRun();
        AxisList axisList = new AxisList(new Axis("script", dockerComposeContainerName));
        if (isParallelized()) {
            final Set commandKeys = ((Map) this.config.get("run")).keySet();
            axisList = new AxisList(new Axis("script", new ArrayList<>(commandKeys)));
        }
        return axisList;
    }

    public String getOnlyRun() {
        final Map runConfig = (Map) this.config.get("run");

        return (String) runConfig.keySet().iterator().next();
    }

    public boolean isParallelized() {
        return ((Map) this.config.get("run")).size() > 1;
    }

    public ShellCommands getCopyWorkDirIntoWorkspaceCommands(final String run, final String projectName) {
        final ShellCommands copyCommands = new ShellCommands(false);
        copyCommands.add(String.format("if docker inspect %s_%s_1 &>/dev/null ; then containerName=%s_%s_1 ; else containerName=%s_%s_run_1 ; fi ; export containerName", projectName, run, projectName, run, projectName, run));
        copyCommands.add("export workingDir=`docker inspect -f '{{ .Config.WorkingDir }}' $containerName | sed -e 's|^/||g'`");
        copyCommands.add("stripComponents=0 ; if [ ! \"x\" == \"x$workingDir\" ]; then set +e ; (( stripComponents+=1 )) ; set -e ; fi ; export stripComponents");
        copyCommands.add("numOfSlashes=`grep -o \"/\" <<< \"$workingDir\" | wc -l` ; set +e ; (( stripComponents+=numOfSlashes )) ; set -e ; export stripComponents");
        copyCommands.add("docker export $containerName | tar --no-same-owner --no-same-permissions --exclude=proc --exclude=dev -x ${workingDir} --strip-components=${stripComponents}");
        return copyCommands;
    }

    public List<DotCiPluginAdapter> getPlugins() {
        final List plugins = this.config.get("plugins") != null ? (List) this.config.get("plugins") : Collections.emptyList();
        return new DotCiExtensionsHelper().createPlugins(plugins);
    }

    public List<PostBuildNotifier> getNotifiers() {
        final List notifiers = this.config.get("notifications") != null ? (List) this.config.get("notifications") : Collections.emptyList();
        return new DotCiExtensionsHelper().createNotifiers(notifiers);
    }

    public String getDockerComposeFileName() {
        return this.config.get("docker-compose-file") != null ? (String) this.config.get("docker-compose-file") : "docker-compose.yml";
    }

    private void appendCommands(final String key, final ShellCommands commands) {
        final ShellCommands added = getShellCommands(key);
        if (added != null) {
            commands.add(added);
        }
    }

    private ShellCommands getShellCommands(final String key) {
        final Object value = this.config.get(key);
        if (value == null) {
            return null;
        }

        final ShellCommands commands = new ShellCommands();
        if (value instanceof String) {
            commands.add((String) value);
        } else if (value instanceof List) {
            final List l = (List) value;

            for (final Object v : l) {
                if (!(v instanceof String)) {
                    throw new RuntimeException(String.format("Unexpected type: %s. Expected String for key: %s", v.getClass().getName(), key));
                }
                commands.add((String) v);
            }
        }
        return commands;
    }

    public boolean isSkipped() {
        return this.config.containsKey("skip");
    }
}
