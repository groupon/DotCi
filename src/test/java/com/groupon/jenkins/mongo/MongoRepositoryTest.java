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
package com.groupon.jenkins.mongo;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import com.groupon.jenkins.dynamic.buildtype.BuildTypeProperty;
import com.groupon.jenkins.github.services.GithubAccessTokenRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import hudson.model.JobProperty;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.jenkinsci.plugins.GithubAuthenticationToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

import org.kohsuke.github.*;
import org.mongodb.morphia.Datastore;
import org.powermock.api.mockito.PowerMockito;

import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MongoRepositoryTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @After
    public void clearFongo() {
        Datastore ds = SetupConfig.get().getDynamicBuildRepository().getDatastore();

        ds.delete(ds.createQuery(DbBackedProject.class));
        ds.delete(ds.createQuery(DbBackedBuild.class));
        ds.getDB().getCollection(GithubAccessTokenRepository.COLLECTION_NAME).remove(new BasicDBObject());
    }

    @Test
    @LocalData
    public void should_save_a_project() throws Exception {
        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();

        GHRepository ghRepository = setupMockGHRepository();

        DynamicProject project = repo.createNewProject(ghRepository);
        project.addProperty(new CyclicProperty(project));
        project.save();

        assert(repo.getDatastore().getCount(DynamicProject.class) > 0);
        DynamicProject restoredProject = repo.getDatastore().createQuery(DynamicProject.class).get();

        assert("repo_name".equals(restoredProject.getName()));
    }

    @Test
    @LocalData
    @Ignore
    public void should_save_a_build() throws Exception {

        GHRepository ghRepository = setupMockGHRepository();

        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();
        DynamicProject project = repo.createNewProject(ghRepository);

        project.scheduleBuild2(0).get();

        assert(repo.getDatastore().getCount(DbBackedBuild.class) > 0);

        for(DbBackedBuild build : SetupConfig.get().getDynamicBuildRepository().getBuilds(project)) {
            assertNotNull(build.getParent());
            assertNotNull(build.getState());
            assertNotNull(build.getResult());
            assertNotNull(build.getNumber());
            assertNotNull(build.getCurrentBranch());
            assertNotNull(build.getPusher());
            assertNotNull(build.getSha());
            assertNotNull(build.getBuiltOn());
            assertNotNull(build.getTime());
        }

    }

    // TODO Organize this into a utility class for greater reusablility
    private GHRepository setupMockGHRepository() throws Exception{
        GHRepository ghRepository = PowerMockito.mock(GHRepository.class);

        PowerMockito.whenNew(GHRepository.class).withNoArguments().thenReturn(ghRepository);
        PowerMockito.when(ghRepository.getHooks()).thenReturn(new ArrayList<GHHook>());
        PowerMockito.when(ghRepository.getUrl()).thenReturn("git@github.com:groupon/DotCi.git");

        GHHook hook = new GHHook();
        PowerMockito.when(ghRepository.createHook("web", new HashMap<String, String>() {{
            put("url", "http://localhost/githook/");
        }}, Arrays.asList(GHEvent.PUSH, GHEvent.PULL_REQUEST), true)).thenReturn(hook);
        PowerMockito.when(ghRepository.isPrivate()).thenReturn(true);
        PowerMockito.when(ghRepository.getDeployKeys()).thenReturn(new ArrayList<GHDeployKey>());
        PowerMockito.when(ghRepository.addDeployKey("DotCi", null)).thenReturn(null);
        PowerMockito.when(ghRepository.getName()).thenReturn("repo_name");

        GHUser ghUser = PowerMockito.mock(GHUser.class);
        PowerMockito.when(ghUser.getLogin()).thenReturn("theusername");
        PowerMockito.when(ghRepository.getOwner()).thenReturn(ghUser);

        String dotCiYaml = "environment:\n  language: ruby\n\nbuild:\n  before: echo \"get out of here denton\"\n  run:\n    unit: echo \"Unit test\"\n    integration: echo \"Integration test\"\n  after: echo it works right\n";
        GHContent content = PowerMockito.mock(GHContent.class);
        PowerMockito.when(content.getContent()).thenReturn(dotCiYaml);
        PowerMockito.when(ghRepository.getFileContent(".ci.yml", "thisisasha")).thenReturn(content);

        GHRef ghRef = PowerMockito.mock(GHRef.class);
        GHRef.GHObject ghObject = PowerMockito.mock(GHRef.GHObject.class);
        PowerMockito.when(ghObject.getSha()).thenReturn("thisisasha");
        PowerMockito.when(ghRef.getObject()).thenReturn(ghObject);

        PowerMockito.when(ghRepository.getRef("heads/master")).thenReturn(ghRef);

        GHMyself myself = PowerMockito.mock(GHMyself.class);
        PowerMockito.when(myself.getLogin()).thenReturn("someloginstuff");

        PowerMockito.mockStatic(GitHub.class);
        GitHub github = PowerMockito.mock(GitHub.class);
        //PowerMockito.when(GitHub.connectUsingOAuth("https://localhost/api/v3", "thisismytoken")).thenReturn(github);
        PowerMockito.when(github.getMyself()).thenReturn(myself);
        PowerMockito.when(github.getRepository("groupon/DotCi")).thenReturn(ghRepository);

        GithubAuthenticationToken token = PowerMockito.mock(GithubAuthenticationToken.class);
        PowerMockito.when(token.getGitHub()).thenReturn(github);
        PowerMockito.when(token.getAccessToken()).thenReturn("thisismytoken");
        PowerMockito.when(token.getName()).thenReturn("thisismyname");

        SecurityContext context = PowerMockito.mock(SecurityContext.class);
        PowerMockito.when(context.getAuthentication()).thenReturn(token);
        SecurityContextHolder.setContext(context);

        return ghRepository;
    }
}

class CyclicProperty extends JobProperty<DynamicProject> {
    private DynamicProject project;
    private ArbitraryCycleClass cyclicObject;
    CyclicProperty(DynamicProject project) {
        super();
        this.project = project;
        cyclicObject = new ArbitraryCycleClass();
    }
}

class ArbitraryCycleClass {
    private ArbitraryCycleClass cyclicObject;
    ArbitraryCycleClass() {
        cyclicObject = this;
    }
}