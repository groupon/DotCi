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

import com.groupon.jenkins.buildtype.InvalidBuildConfigurationException;
import com.groupon.jenkins.buildtype.install_packages.template.DotCiTemplate;
import com.groupon.jenkins.github.services.GithubRepositoryService;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import org.kohsuke.github.GHContent;

public class BuildConfigurationCalculator {

    public BuildConfiguration calculateBuildConfiguration(String githubRepoUrl, String sha, Map<String,Object> envVars) throws IOException, InterruptedException, InvalidBuildConfigurationException {
        GithubRepositoryService githubRepositoryService = getGithubRepositoryService(githubRepoUrl);
        DotCiTemplate dotCiTemplate = new DotCiTemplate();
        try {
            GHContent file = githubRepositoryService.getGHFile(".ci.yml", sha);
            BuildConfiguration configuration = new BuildConfiguration(file.getContent(), envVars);
            if (!configuration.isValid()) {
                throw new InvalidBuildConfigurationException(configuration.getValidationErrors());
            }
          if(configuration.getLanguage() == null){
              DotCiTemplate defaultParentTemplate = dotCiTemplate.getDefaultFor(githubRepositoryService.getGithubRepository()); //.getBuildConfiguration(envVars);
              return dotCiTemplate.getMergedTemplate(configuration,defaultParentTemplate,envVars);
          }
            return dotCiTemplate.getMergedTemplate(configuration, configuration.getLanguage(), envVars);
        } catch (FileNotFoundException e) {
            return dotCiTemplate.getDefaultFor(githubRepositoryService.getGithubRepository()).getBuildConfiguration(envVars);
        }
    }

    protected GithubRepositoryService getGithubRepositoryService(String githubRepoUrl) {
        return new GithubRepositoryService(githubRepoUrl);
    }

}
