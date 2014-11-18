package com.groupon.jenkins.mongo;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.RuleChain;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.kohsuke.github.GHRepository;

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
    @Ignore
    public void should_find_project_for_a_url() {
    }

    @Test
    @LocalData
    @Ignore
    public void should_create_a_new_project() {
    }

    @Test
    @LocalData
    @Ignore
    public void should_check_if_a_project_exists() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_find_a_project_by_id() {
    }
}
