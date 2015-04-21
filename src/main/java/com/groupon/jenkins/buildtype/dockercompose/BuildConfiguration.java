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

import com.groupon.jenkins.buildtype.docker.CheckoutCommands;
import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import com.groupon.jenkins.extensions.DotCiExtensionsHelper;
import com.groupon.jenkins.git.GitUrl;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;

import java.util.*;

import static java.lang.String.format;

public class BuildConfiguration {
    private final String dockerComposeProjectName;
    private String repoName;
    private ShellCommands checkoutCommands;
    private String sha;
    private int number;
    private Map config;
    private String buildId;

    public static final Escaper SHELL_ESCAPE;
    static {
        final Escapers.Builder builder = Escapers.builder();
        builder.addEscape('\'', "'\"'\"'");
        SHELL_ESCAPE = builder.build();
    }

    public BuildConfiguration(String repoName, Map config, String buildId, ShellCommands checkoutCommands, String sha, int number) {
        this.repoName = repoName;
        this.sha = sha;
        this.number = number;
        this.dockerComposeProjectName = repoName.replaceAll("[^A-Za-z0-9]", "").replaceAll("$", String.valueOf(number)).toLowerCase();
        this.config = config;
        this.buildId = buildId;
        this.checkoutCommands = checkoutCommands;
    }

    public ShellCommands getCommands(Combination combination) {
        String dockerComposeContainerName = combination.get("run");
        String projectName = dockerComposeContainerName + this.dockerComposeProjectName;
        ShellCommands shellCommands = new ShellCommands();
        shellCommands.add(checkoutCommands);
        shellCommands.add(String.format("trap \"docker-compose -p %s kill; docker-compose -p %s rm --force; exit\" PIPE QUIT INT HUP EXIT TERM",projectName,projectName));
        shellCommands.add(String.format("docker-compose -p %s pull",projectName));
        shellCommands.add(String.format("docker-compose -p %s build",projectName));
        if (config.get("run") != null) {
            Map runConfig = (Map) config.get("run");
            Object dockerComposeCommand = runConfig.get(dockerComposeContainerName);
            if (dockerComposeCommand != null ) {
                shellCommands.add(String.format("docker-compose -p %s run -T %s sh -xc '%s'", projectName, dockerComposeContainerName,SHELL_ESCAPE.escape((String) dockerComposeCommand)));
            }
            else {
                shellCommands.add(String.format("docker-compose -p %s run -T %s",projectName,dockerComposeContainerName));
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
        AxisList  axisList = new AxisList(new Axis("run",dockerComposeContainerName));
        if (isParallelized()) {
            Set commandKeys =  ((Map) config.get("run")).keySet();
            axisList = new AxisList(new Axis("run", new ArrayList<String>(commandKeys)));
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
}
