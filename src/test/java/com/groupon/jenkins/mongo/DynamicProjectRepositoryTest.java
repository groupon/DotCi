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
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.rules.RuleChain;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;

public class DynamicProjectRepositoryTest {
    @Rule
    public RuleChain chain = RuleChain
            .outerRule(new JenkinsRule())
            .around(new MongoDataLoadRule());

    private DynamicProjectRepository repo;

    @Before
    public void setupRepo() {
        repo = SetupConfig.get().getDynamicProjectRepository();
    }

    @Test
    @LocalData
    @Ignore
    public void should_save_or_update_a_project() {


    }

    @Test
    @LocalData
    @Ignore
    public void should_find_a_child() {
    }

    @Test
    @LocalData
    @Ignore
    public void should_find_its_children() {

    }

    @Test
    @LocalData
    public void should_delete_a_project() {
        assertEquals(1L, repo.getDatastore().createQuery(DynamicProject.class).countAll());
        DynamicProject project = repo.getProjectById(new ObjectId("5451e5ee30047b534b7bd50b"));
        repo.delete(project);
        assertEquals(0L, repo.getDatastore().createQuery(DynamicProject.class).countAll());
    }

    @Test
    @LocalData
    @Ignore // TODO requires regular Jenkins initialization to search. This query is not actually made through MongoDB.
    public void should_find_project_for_a_url() {
        String testUrl = "https://localhost/test_group/test_job";
        Iterable<DynamicProject> projects = repo.getJobsFor(testUrl);
        assertTrue("Unable to find any projects with test url", projects.iterator().hasNext()); // there is at least one item
        for(DynamicProject project : projects) {
            assertEquals(testUrl, project.getUrl());
        }
    }

    @Test
    @LocalData
    @Ignore
    public void should_create_a_new_project() {
    }

    @Test
    @LocalData
    @Ignore // TODO requires regular Jenkins initialization to search. This query is not actually made through MongoDB.
    public void should_check_if_a_project_exists() throws Exception {
        GHRepository ghRepository = mock(GHRepository.class);
        GHUser ghUser = mock(GHUser.class);
        when(ghUser.getLogin()).thenReturn("testuser");
        when(ghRepository.getOwner()).thenReturn(ghUser);
        when(ghRepository.getName()).thenReturn("test_job");

        assertTrue(repo.projectExists(ghRepository));

    }

    @Test
    @LocalData
    @Ignore
    public void should_find_a_project_by_id() {
    }
}
