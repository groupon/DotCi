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
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicSubProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import jenkins.model.Jenkins;
import org.assertj.core.util.Lists;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
    public void should_save_or_update_a_project() throws Exception {
        ObjectId id = new ObjectId("5451e5ee30047b534b7bd50b");
        DynamicProject project = repo.getProjectById(id);
        assertEquals("test_job", project.getDisplayName());
        project.setDisplayName("someothername"); // saves internally
        DynamicProject savedProject = repo.getProjectById(id);
        assertEquals("someothername", savedProject.getDisplayName());
    }

    @Test
    @LocalData
    public void should_find_a_child() {
        ObjectId parentId = new ObjectId("54c6a3cd1c265eb3a07d58b4");
        String name = "script=integration";
        DynamicProject parent = repo.getProjectById(parentId);
        DynamicSubProject child = repo.getChild(parent, name);
        assertNotNull(child);
        assertEquals(name, child.getName());
        assertEquals(parentId, child.getParent().getId());
    }

    @Test
    @LocalData
    public void should_find_its_children() {
        ObjectId parentId = new ObjectId("54c6a3cd1c265eb3a07d58b4");
        DynamicProject parent = repo.getProjectById(parentId);
        List<DynamicSubProject> children = Lists.newArrayList(repo.getChildren(parent));
        assertEquals(3, children.size()); // database has three sub-builds
        for (DynamicSubProject subProject : children) {
            assertEquals(parentId, subProject.getParent().getId());
        }
    }

    @Test
    @LocalData
    public void should_delete_a_project() {
        assertEquals(1L, repo.getDatastore().createQuery(DynamicProject.class).countAll());
        DynamicProject project = spy(repo.getProjectById(new ObjectId("5451e5ee30047b534b7bd50b")));
        OrganizationContainer parent = new OrganizationContainer(Jenkins.getInstance(), "groupon");
        doReturn(parent).when(project).getParent();
        repo.delete(project);
        assertEquals(0L, repo.getDatastore().createQuery(DynamicProject.class).countAll());
    }

    @Test
    @LocalData
    public void should_find_project_for_a_url() {
        String testUrl = "https://localhost/test_group/test_job";
        Iterable<DynamicProject> projects = repo.getJobsFor(testUrl);
        assertTrue("Unable to find any projects with test url", projects.iterator().hasNext()); // there is at least one item
        for (DynamicProject project : projects) {
            assertEquals(testUrl, project.getGithubRepoUrl());
        }
    }

    @Test
    @LocalData
    public void should_create_a_new_project() throws Exception {
        GHRepository ghRepository = mock(GHRepository.class);
        when(ghRepository.getHtmlUrl()).thenReturn(new URL("http://github.com/meow"));
        GHUser ghUser = mock(GHUser.class);
        when(ghUser.getLogin()).thenReturn("test_user");
        when(ghRepository.getOwner()).thenReturn(ghUser);
        when(ghRepository.getName()).thenReturn("test_job");

        int totalBefore = (int) repo.getDatastore().createQuery(DynamicProject.class).countAll();

        DynamicProject project = repo.createNewProject(ghRepository, "test", "username");
        assertNotNull(project);
        DynamicProject retrieved = repo.getProjectById(project.getId());
        assertNotNull(retrieved);
        int totalAfter = (int) repo.getDatastore().createQuery(DynamicProject.class).countAll();
        assertEquals(totalAfter, totalBefore + 1);
    }

    @Test
    @LocalData
    public void should_check_if_a_project_exists() throws Exception {
        GHRepository ghRepository = mock(GHRepository.class);
        GHUser ghUser = mock(GHUser.class);
        when(ghUser.getLogin()).thenReturn("test_user");
        when(ghRepository.getOwner()).thenReturn(ghUser);
        when(ghRepository.getName()).thenReturn("test_job");

        assertTrue(repo.projectExists(ghRepository));
    }

    @Test
    @LocalData
    public void should_find_a_project_by_id() throws Exception {
        ObjectId id = new ObjectId("5451e5ee30047b534b7bd50b");
        DynamicProject project = repo.getProjectById(id);
        assertEquals(id, project.getId());
    }
}
