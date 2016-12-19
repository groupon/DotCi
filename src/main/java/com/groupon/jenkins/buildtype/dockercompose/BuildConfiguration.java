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

import com.groupon.jenkins.buildtype.dockercompose.buildconfiguration.ComposeFileNameSection;
import com.groupon.jenkins.buildtype.dockercompose.buildconfiguration.GenericCommandsSection;
import com.groupon.jenkins.buildtype.dockercompose.buildconfiguration.NotificationsSection;
import com.groupon.jenkins.buildtype.dockercompose.buildconfiguration.PluginsSection;
import com.groupon.jenkins.buildtype.dockercompose.buildconfiguration.RunSection;
import com.groupon.jenkins.buildtype.dockercompose.buildconfiguration.SkipSection;
import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import com.groupon.jenkins.extensions.DotCiExtensionsHelper;
import com.groupon.jenkins.git.GitUrl;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class BuildConfiguration {

    private final SkipSection skipSection;
    private GenericCommandsSection afterEachSection;
    private GenericCommandsSection afterRunSection;
    private GenericCommandsSection beforeRunSection;
    private GenericCommandsSection beforeSection;
    private GenericCommandsSection beforeEachSection;
    private ComposeFileNameSection composeFileNameSection;
    private NotificationsSection notificationsSection;
    private PluginsSection pluginsSection;
    private RunSection runSection;

    public BuildConfiguration(final Map config) {
        this.skipSection = new SkipSection(config.get(SkipSection.KEY));
        if (isSkipped())
            return; //don't initialize/validate other sections if skipped
        this.runSection = new RunSection(config.get(RunSection.KEY));
        this.pluginsSection = new PluginsSection(config.get(PluginsSection.KEY));
        this.notificationsSection = new NotificationsSection(config.get(NotificationsSection.KEY));
        this.composeFileNameSection = new ComposeFileNameSection(config.get(ComposeFileNameSection.KEY));
        this.beforeEachSection = new GenericCommandsSection(config.get("before_each"));
        this.beforeSection = new GenericCommandsSection(config.get("before"));
        this.beforeRunSection = new GenericCommandsSection(config.get("before_run"));
        this.afterRunSection = new GenericCommandsSection(config.get("after_run"));
        this.afterEachSection = new GenericCommandsSection(config.get("after_each"));
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
        return new ShellCommands(this.beforeRunSection.getCommands());
    }

    public ShellCommands getAfterRunCommandsIfPresent() {
        return new ShellCommands(this.afterRunSection.getCommands());
    }

    public List<ShellCommands> getCommands(final Combination combination, final Map<String, Object> dotCiEnvVars) {
        final List<ShellCommands> allCommands = new ArrayList<>();
        final String dockerComposeContainerName = combination.get("script");
        final String projectName = (String) dotCiEnvVars.get("COMPOSE_PROJECT_NAME");
        final String fileName = getDockerComposeFileName();

        final ShellCommands shellCommands = new ShellCommands();
        shellCommands.add(BuildConfiguration.getCheckoutCommands(dotCiEnvVars));

        shellCommands.addAll(this.beforeEachSection.getCommands());
        shellCommands.addAll(this.beforeSection.getCommands());//deprecated

        shellCommands.add(String.format("docker-compose -f %s pull", fileName));


        shellCommands.addAll(this.runSection.getCommands(dockerComposeContainerName, fileName));

        shellCommands.addAll(this.afterEachSection.getCommands());
        if (!isParallelized()) {
            shellCommands.addAll(this.afterRunSection.getCommands());
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


    public AxisList getAxisList() {
        return this.runSection.getAxisList();
    }


    public boolean isParallelized() {
        return this.runSection.isParallelized();
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
        return new DotCiExtensionsHelper().createPlugins(this.pluginsSection.getPlugins());
    }

    public List<PostBuildNotifier> getNotifiers() {
        return new DotCiExtensionsHelper().createNotifiers(this.notificationsSection.getNotifiers());
    }

    public String getDockerComposeFileName() {
        return this.composeFileNameSection.getComposeFileName();
    }


    public boolean isSkipped() {
        return this.skipSection.isSkipped();
    }
}
