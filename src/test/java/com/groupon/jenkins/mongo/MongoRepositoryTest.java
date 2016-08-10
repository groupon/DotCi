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
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import hudson.model.JobProperty;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHDeployKey;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.powermock.api.mockito.PowerMockito;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MongoRepositoryTest {


    @Rule
    public RuleChain chain = RuleChain
            .outerRule(new JenkinsRule())
            .around(new MongoDataLoadRule());

    @Test
    @LocalData
    public void should_save_a_project() throws Exception {
        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();

        GHRepository ghRepository = setupMockGHRepository();

        DynamicProject project = repo.createNewProject(ghRepository,null,null);

        project.addProperty(new CyclicProperty(project));
        project.save();

        assertTrue(repo.getDatastore().getCount(DynamicProject.class) > 0);
        DynamicProject restoredProject = repo.getDatastore().createQuery(DynamicProject.class).get();

        assertEquals("repo_name", restoredProject.getName());
    }

    @Test
    @LocalData
    public void should_save_mixed_type_lists() {
        MixedTypeListClass mixed = new MixedTypeListClass();
        Serializable [] mixedList = {1, "teststring", new DummySerialiazable()};
        mixed.mixedTypeList = mixedList;
        Datastore ds = SetupConfig.get().getInjector().getInstance(Datastore.class);
        ds.save(mixed);
        MixedTypeListClass restoredMixed = ds.createQuery(MixedTypeListClass.class).get();

        assertEquals(1, restoredMixed.mixedTypeList[0]);
        assertEquals("teststring", restoredMixed.mixedTypeList[1]);
        assertNotNull(restoredMixed.mixedTypeList[2]);
        assertTrue(restoredMixed.mixedTypeList[2] instanceof DummySerialiazable);
        DummySerialiazable dummy = (DummySerialiazable)restoredMixed.mixedTypeList[2];
        assertEquals("test", dummy.test);
    }

    @Test
    @LocalData
    @Ignore
    public void should_save_a_build() throws Exception {

        GHRepository ghRepository = setupMockGHRepository();

        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();
        DynamicProject project = repo.createNewProject(ghRepository,null,null);

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
        PowerMockito.when(ghRepository.getHtmlUrl()).thenReturn(new URL("https://github.com/groupon/DotCi"));

        GHHook hook = PowerMockito.mock(GHHook.class);
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



        SecurityContext context = PowerMockito.mock(SecurityContext.class);
//        PowerMockito.when(context.getAuthentication()).thenReturn(token);
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

@Entity("test")
class MixedTypeListClass {
    @Id
    public ObjectId id;
    public Serializable[] mixedTypeList;
}

class DummySerialiazable implements Serializable {
    public String test = "test";
}

class ArbitraryCycleClass {
    private ArbitraryCycleClass cyclicObject;
    ArbitraryCycleClass() {
        cyclicObject = this;
    }
}
