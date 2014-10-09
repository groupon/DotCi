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
package com.groupon.jenkins.buildtype.install_packages.buildconfiguration;

import com.groupon.jenkins.buildtype.dockerimage.DockerCommandBuilder;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ListOrSingleValue;
import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.MapValue;
import com.groupon.jenkins.buildtype.util.shell.ShellCommands;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.groupon.jenkins.buildtype.dockerimage.DockerCommandBuilder.dockerCommand;
import static java.lang.String.format;

public class EnvironmentSection extends CompositeConfigSection {

    public static final String NAME = "environment";
    private final LanguageVersionsSection languageVersionsSection;
    private final LanguageSection languageSection;
    private final ImageSection imageSection;
    private final VarsSection varsSection;
    private final PackagesSection packagesSection;
    private final ServicesSection servicesSection;

    public EnvironmentSection(MapValue<String, ?> config) {
        super(NAME, config);
        imageSection = new ImageSection(getSectionConfig(ImageSection.NAME, com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.StringValue.class));
        languageVersionsSection = new LanguageVersionsSection(getSectionConfig(LanguageVersionsSection.NAME, ListOrSingleValue.class));
        languageSection = new LanguageSection(getSectionConfig(LanguageSection.NAME, com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.StringValue.class));
        this.varsSection = new VarsSection(getSectionConfig(VarsSection.NAME, MapValue.class));
        this.servicesSection = new ServicesSection(getSectionConfig(ServicesSection.NAME, ListOrSingleValue.class));

        packagesSection = new PackagesSection(getSectionConfig(PackagesSection.NAME, ListOrSingleValue.class), languageSection, languageVersionsSection);
        setSubSections(packagesSection, imageSection, varsSection, languageSection, languageVersionsSection, servicesSection);
    }

    public boolean isMultiLanguageVersions() {
        return languageVersionsSection.isMultiLanguageVersions();
    }

    public List<String> getLanguageVersions() {
        return languageVersionsSection.getLanguageVersions();
    }

    public String getLanguage() {
        return languageSection.getLanguage();
    }

    public PackagesSection getPackagesSection() {
        return packagesSection;
    }

    public ShellCommands getDockerBuildRunScriptForImage(String buildId, Map<String, String> envVars) {
        ShellCommands commands = new ShellCommands();
        String dockerImageName = imageSection.getImageName(buildId);
        commands.add(imageSection.getDockerCommand(buildId));

        /* @formatter:off */
        DockerCommandBuilder runCommand = dockerCommand("run")
                                          .flag("rm")
                                          .flag("sig-proxy=true")
                                          .flag("v", "`pwd`:/var/project")
                                          .flag("w", "/var/project")
                                          .flag("u", "`id -u`")
                                          .args(dockerImageName, "/bin/bash -e dotci_build_script.sh");
        
        exportEnvVars(runCommand, envVars);
        /* @formatter:on */
        if (servicesSection.isSpecified()) {
            commands.addAll(servicesSection.getServiceStartCommands(buildId));
            for (String link : servicesSection.getContainerLinkCommands(buildId)) {
                runCommand.flag("link", link);
            }
        }
        commands.add(runCommand.get());
        return commands;
    }

    private void exportEnvVars(DockerCommandBuilder runCommand, Map<String, String> envVars) {
        for (Entry<String, String> var : envVars.entrySet()) {
            runCommand.flag("e", format("\"%s=%s\"", var.getKey(), var.getValue()));
        }
    }

    public String getCleanupScript(String buildId) {
        if (servicesSection.isSpecified()) {
            List<String> commands = servicesSection.getCleanupCommands(buildId);
            return new ShellCommands(commands).toShellScript();
        }
        return "true";
    }

    public boolean isDocker() {
        return imageSection.isSpecified();
    }

    public ShellCommands getDockerBuildRunScriptForLocal(String buildId, Map<String, String> envVars, String buildCommand) {
        ShellCommands commands = new ShellCommands();
        String buildImageCommand = dockerCommand("build").flag("t").args(buildId, ".").get();
        commands.add(buildImageCommand);

        // If we are running a command in the container and we have services to link to, try socat
        if (servicesSection.isSpecified()) {
            buildCommand = buildCommandAmbassador(buildCommand);
        }

        /* @formatter:off */
        DockerCommandBuilder runCommand = dockerCommand("run")
                                          .flag("rm")
                                          .flag("sig-proxy=true")
                                          .args(buildId, buildCommand);
        
        exportEnvVars(runCommand, envVars);
        /* @formatter:on */
        if (servicesSection.isSpecified()) {
            commands.addAll(servicesSection.getServiceStartCommands(buildId));
            for (String link : servicesSection.getContainerLinkCommands(buildId)) {
                runCommand.flag("link", link);
            }
        }
        commands.add(runCommand.get());
        return commands;
    }

    public String buildCommandAmbassador(String buildCommand) {
        String shellPrefix = "sh -c \"env && ";
        if (buildCommand.contains(shellPrefix)) {
            int defaultEnvLength = shellPrefix.length();
            return new StringBuilder(buildCommand).insert(defaultEnvLength, DEFAULT_LINK_PROXY).toString();
        }
        return buildCommand;
    }

    // See the ambassador pattern :  https://docs.docker.com/articles/ambassador_pattern_linking/
    public final String DEFAULT_LINK_PROXY = "if [ -x /usr/bin/socat ]; then env | grep _TCP= | sed 's/.*_PORT_\\" +
            "([0-9]*\\)_TCP=tcp:\\/\\/\\(.*\\):\\(.*\\)/socat TCP4-LISTEN:\\1,fork,reuseaddr TCP4:\\2:\\3 \\&/' " +
            "| sh ;fi && ";
}
