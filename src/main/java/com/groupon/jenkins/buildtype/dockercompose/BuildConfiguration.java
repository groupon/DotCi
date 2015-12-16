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
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.groupon.jenkins.git.*;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;

import java.util.*;

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

    public ShellCommands getBeforeRunCommandIfPresent() {
        if (!config.containsKey("before_run")) {
            return null;
        }
        ShellCommands shellCommands = new ShellCommands();
        shellCommands.add(String.format("sh -xc '%s'", SHELL_ESCAPE.escape((String) config.get("before_run"))));
        return shellCommands;
    }

    public ShellCommands getCommands(Combination combination, Map<String, Object> dotCiEnvVars) {
        String dockerComposeContainerName = combination.get("script");
        String projectName = (String) dotCiEnvVars.get("COMPOSE_PROJECT_NAME");
        String fileName = getDockerComposeFileName();

        ShellCommands shellCommands = new ShellCommands();
        shellCommands.add(BuildConfiguration.getCheckoutCommands(dotCiEnvVars));

        //TODO consider piping to /dev/null here to avoid docker errors from confusing users
        shellCommands.add(String.format("trap \"docker-compose -f %s kill; docker ps -a --filter 'name=%s_' -q | xargs docker rm -v --force || true; docker images | grep %s_ | awk '{print $3}' | xargs docker rmi --force || true; exit\" PIPE QUIT INT HUP EXIT TERM", fileName, projectName, projectName));
        if (config.containsKey("before_run") && !isParallelized()) {
            shellCommands.add(String.format("sh -xc '%s'", SHELL_ESCAPE.escape((String) config.get("before_run"))));
        }

        if (config.containsKey("before_each") || config.containsKey("before")) {
            String beforeScript = (String) (config.containsKey("before_each") ? config.get("before_each") : config.get("before"));
            shellCommands.add(String.format("sh -xc '%s'", SHELL_ESCAPE.escape(beforeScript)));
        }

        shellCommands.add(String.format("docker-compose -f %s pull",fileName));
        if (config.get("run") != null) {
            Map runConfig = (Map) config.get("run");
            Object dockerComposeCommand = runConfig.get(dockerComposeContainerName);
            if (dockerComposeCommand != null ) {
                shellCommands.add(String.format("docker-compose -f %s run -T %s %s", fileName, dockerComposeContainerName,SHELL_ESCAPE.escape((String) dockerComposeCommand)));
            }
            else {
                shellCommands.add(String.format("docker-compose -f %s run -T %s",fileName,dockerComposeContainerName));
            }
        }
        extractWorkingDirIntoWorkSpace(dockerComposeContainerName, projectName, shellCommands);

        return shellCommands;
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
            //TODO Handle dockerfiles with onbuild add directives
            shellCommands.add("( for dockerfile in $(find . -name '*Dockerfile*'); do dockerfileName=$(basename $dockerfile) ; dockerfilePath=$(dirname $dockerfile); pushd $dockerfilePath >/dev/null ; for filename in $(grep ADD $dockerfileName | sed -e 's/^\\s*ADD\\s*//g' ; grep COPY $dockerfileName | sed -e 's/^\\s*COPY\\s*//g' ); do test -d $filename && continue ; test -f $filename || continue ; sha=$(git rev-list -n 1 HEAD $filename) ; touch -d \"$(git show -s --format=%ai $sha)\" $filename ; popd >/dev/null ; done ; done ) || true # Set modified time of files added in Dockerfiles to allow for build caching");
        }
        return shellCommands;
    }

    public boolean isSkipped() {
        return config.containsKey("skip");
    }
}
