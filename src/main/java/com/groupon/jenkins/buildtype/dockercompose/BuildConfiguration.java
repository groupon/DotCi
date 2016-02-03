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

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
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

    private Map config;

    public static final Escaper SHELL_ESCAPE;
    static {
        final Escapers.Builder builder = Escapers.builder();
        builder.addEscape('\'', "'\"'\"'");
        SHELL_ESCAPE = builder.build();
    }

    public BuildConfiguration(Map config) {
        this.config = config;
    }

    public ShellCommands getBeforeRunCommandsIfPresent() {
        return getShellCommands("before_run");
    }

    public ShellCommands getAfterRunCommandsIfPresent() {
        return getShellCommands("after_run");
    }

    public ShellCommands getCommands(Combination combination, Map<String, Object> dotCiEnvVars) {
        String dockerComposeContainerName = combination.get("script");
        String projectName = (String) dotCiEnvVars.get("COMPOSE_PROJECT_NAME");
        String fileName = getDockerComposeFileName();

        ShellCommands shellCommands = new ShellCommands();
        shellCommands.add(BuildConfiguration.getCheckoutCommands(dotCiEnvVars));

        shellCommands.add(String.format("trap \"docker-compose -f %s kill; docker-compose -f %s rm -v --force; exit\" PIPE QUIT INT HUP EXIT TERM",fileName,fileName));
        if (config.containsKey("before_each") || config.containsKey("before")) {
            String key = (config.containsKey("before_each") ? "before_each" : "before");
            appendCommands(key, shellCommands);
        }

        shellCommands.add(String.format("docker-compose -f %s pull",fileName));
        if (config.get("run") != null) {
            Map runConfig = (Map) config.get("run");
            String dockerComposeRunCommand = getDockerComposeRunCommand(dockerComposeContainerName, fileName, runConfig);
            shellCommands.add(format("export COMPOSE_CMD='%s'",dockerComposeRunCommand));
            shellCommands.add(" set +e && hash unbuffer >/dev/null 2>&1 ;  if [ $? = 0 ]; then set -e && unbuffer $COMPOSE_CMD ;else set -e && $COMPOSE_CMD ;fi");
        }
        extractWorkingDirIntoWorkSpace(dockerComposeContainerName, projectName, shellCommands);

        if (config.containsKey("after_each")) {
            appendCommands("after_each", shellCommands);
        }
        if (config.containsKey("after_run") && !isParallelized()) {
            appendCommands("after_each", shellCommands);
        }
        return shellCommands;
    }

    private String getDockerComposeRunCommand(String dockerComposeContainerName, String fileName, Map runConfig) {
        Object dockerComposeCommand = runConfig.get(dockerComposeContainerName);
        if (dockerComposeCommand != null ) {
            return String.format("docker-compose -f %s run -T %s %s", fileName, dockerComposeContainerName,SHELL_ESCAPE.escape((String) dockerComposeCommand));
        }
        else {
            return String.format("docker-compose -f %s run %s ",fileName,dockerComposeContainerName);
        }
    }

    private void extractWorkingDirIntoWorkSpace(String dockerComposeContainerName, String projectName, ShellCommands shellCommands) {
        if (config.get("plugins") != null) {
            shellCommands.add(getCopyWorkDirIntoWorkspaceCommands(dockerComposeContainerName, projectName));
        }
    }

    public AxisList getAxisList() {
        String dockerComposeContainerName = getOnlyRun();
        AxisList  axisList = new AxisList(new Axis("script",dockerComposeContainerName));
        if (isParallelized()) {
            Set commandKeys =  ((Map) config.get("run")).keySet();
            axisList = new AxisList(new Axis("script", new ArrayList<String>(commandKeys)));
        }
        return axisList;
    }

    public String getOnlyRun() {
        Map runConfig = (Map) config.get("run");

        return (String) runConfig.keySet().iterator().next();
    }

    public boolean isParallelized() {
        return ((Map) config.get("run")).size() > 1 ;
    }

    public ShellCommands getCopyWorkDirIntoWorkspaceCommands(String run, String projectName) {
        ShellCommands copyCommands = new ShellCommands();
        copyCommands.add(String.format("if docker inspect %s_%s_1 &>/dev/null ; then containerName=%s_%s_1 ; else containerName=%s_%s_run_1 ; fi ; export containerName",projectName,run,projectName,run,projectName,run));
        copyCommands.add("export workingDir=`docker inspect -f '{{ .Config.WorkingDir }}' $containerName | sed -e 's|^/||g'`");
        copyCommands.add("stripComponents=0 ; if [ ! \"x\" == \"x$workingDir\" ]; then set +e ; (( stripComponents+=1 )) ; set -e ; fi ; export stripComponents");
        copyCommands.add("numOfSlashes=`grep -o \"/\" <<< \"$workingDir\" | wc -l` ; set +e ; (( stripComponents+=numOfSlashes )) ; set -e ; export stripComponents");
        copyCommands.add("docker export $containerName | tar --no-same-owner --no-same-permissions --exclude=proc --exclude=dev -x ${workingDir} --strip-components=${stripComponents}");
        return copyCommands;
    }

    public List<DotCiPluginAdapter> getPlugins() {
        List plugins = config.get("plugins") !=null? (List) config.get("plugins") :Collections.emptyList();
        return new DotCiExtensionsHelper().createPlugins(plugins) ;
    }
    public List<PostBuildNotifier> getNotifiers() {
        List notifiers = config.get("notifications") !=null? (List) config.get("notifications") :Collections.emptyList();
        return new DotCiExtensionsHelper().createNotifiers(notifiers) ;
    }

    public String getDockerComposeFileName() {
        return config.get("docker-compose-file") !=null ? (String) config.get("docker-compose-file") : "docker-compose.yml";
    }

    public static ShellCommands getCheckoutCommands(Map<String, Object> dotCiEnvVars) {
        String gitCloneUrl = (String) dotCiEnvVars.get("DOTCI_DOCKER_COMPOSE_GIT_CLONE_URL");
        GitUrl gitRepoUrl = new GitUrl(gitCloneUrl);
        boolean isPrivateRepo = Boolean.parseBoolean((String) dotCiEnvVars.get("DOTCI_IS_PRIVATE_REPO"));
        String gitUrl = isPrivateRepo ? gitRepoUrl.getGitUrl() : gitCloneUrl;
        ShellCommands shellCommands = new ShellCommands();
        shellCommands.add("chmod -R u+w . ; find . ! -path \"./deploykey_rsa.pub\" ! -path \"./deploykey_rsa\" -delete");
        shellCommands.add("git init");
        shellCommands.add(format("git remote add origin %s",gitUrl));

        if(dotCiEnvVars.get("DOTCI_PULL_REQUEST") != null){
            if(isPrivateRepo){

                shellCommands.add(format("ssh-agent bash -c \"ssh-add -D && ssh-add \\%s/deploykey_rsa && git fetch origin '+refs/pull/%s/merge:' \"",dotCiEnvVars.get("WORKSPACE"), dotCiEnvVars.get("DOTCI_PULL_REQUEST")));
            }else {
                shellCommands.add(format("git fetch origin \"+refs/pull/%s/merge:\"", dotCiEnvVars.get("DOTCI_PULL_REQUEST")));
            }
            shellCommands.add("git reset --hard FETCH_HEAD");
        }else {
            if(isPrivateRepo){

                shellCommands.add(format("ssh-agent bash -c \"ssh-add -D && ssh-add \\%s/deploykey_rsa && git fetch origin %s \"",dotCiEnvVars.get("WORKSPACE"), dotCiEnvVars.get("DOTCI_BRANCH")));
            }else{
                shellCommands.add(format("git fetch origin %s",dotCiEnvVars.get("DOTCI_BRANCH")));
            }
            shellCommands.add(format("git reset --hard  %s", dotCiEnvVars.get("SHA")));
        }
        return shellCommands;
    }

    private void appendCommands(String key, ShellCommands commands) {
        ShellCommands added = getShellCommands(key);
        commands.add(added);
    }

    private ShellCommands getShellCommands(String key) {
        Object value = config.get(key);
        if (value == null) {
            return null;
        }

        ShellCommands commands = new ShellCommands();
        if (value instanceof String) {
            commands.add(escapeShellCommand((String) value));
        } else if (value instanceof List) {
            List l = (List) value;

            for (Object v : l) {
                if (!(v instanceof String)) {
                    throw new RuntimeException("Unexpected type. Expected String");
                }
                commands.add(escapeShellCommand((String) v));
            }
        }
        return commands;
    }

    private String escapeShellCommand(String command) {
        return SHELL_ESCAPE.escape(command);
    }

    public boolean isSkipped() {
        return config.containsKey("skip");
    }
}
