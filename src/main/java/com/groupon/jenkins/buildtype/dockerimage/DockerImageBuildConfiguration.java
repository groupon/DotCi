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

package com.groupon.jenkins.buildtype.dockerimage;

import com.groupon.jenkins.buildtype.plugins.DotCiPluginAdapter;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import com.groupon.jenkins.extensions.DotCiExtensionsHelper;
import com.groupon.jenkins.notifications.PostBuildNotifier;
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.Combination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DockerImageBuildConfiguration {

    private Map config;

    public DockerImageBuildConfiguration(Map config) {
        this.config = config;
    }

    public ShellCommands getBeforeRunCommandsIfPresent() {
        return getShellCommands("before_run");
    }

    public ShellCommands getAfterRunCommandsIfPresent() {
        return getShellCommands("after_run");
    }

    public ShellCommands getCommands(Combination combination, Map<String, Object> dotCiEnvVars) {
        String script = combination.get("script");

        ShellCommands shellCommands = new ShellCommands();


        appendCommands("before", shellCommands); //deprecated
        appendCommands("before_each", shellCommands);

        if (config.get("run") != null) {
            Map runConfig = (Map) config.get("run");
            shellCommands.add((String)runConfig.get(script));
        }

        appendCommands("after_each", shellCommands);
        if (!isParallelized()) {
            appendCommands("after_run", shellCommands);
        }
        return shellCommands;
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


    public List<DotCiPluginAdapter> getPlugins() {
        List plugins = config.get("plugins") !=null? (List) config.get("plugins") :Collections.emptyList();
        return new DotCiExtensionsHelper().createPlugins(plugins) ;
    }
    public List<PostBuildNotifier> getNotifiers() {
        List notifiers = config.get("notifications") !=null? (List) config.get("notifications") :Collections.emptyList();
        return new DotCiExtensionsHelper().createNotifiers(notifiers) ;
    }



    private void appendCommands(String key, ShellCommands commands) {
        ShellCommands added = getShellCommands(key);
        if (added != null) {
            commands.add(added);
        }
    }

    private ShellCommands getShellCommands(String key) {
        Object value = config.get(key);
        if (value == null) {
            return null;
        }

        ShellCommands commands = new ShellCommands();
        if (value instanceof String) {
            commands.add((String) value);
        } else if (value instanceof List) {
            List l = (List) value;

            for (Object v : l) {
                if (!(v instanceof String)) {
                    throw new RuntimeException(String.format("Unexpected type: %s. Expected String for key: %s", v.getClass().getName(), key));
                }
                commands.add((String) v);
            }
        }
        return commands;
    }

    public boolean isSkipped() {
        return config.containsKey("skip");
    }
}
