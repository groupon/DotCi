package com.groupon.jenkins.mongo;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.repository.DynamicProjectRepository;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.RuleChain;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class DynamicProjectRepositoryTest {
    @Rule
    public RuleChain chain = RuleChain
            .outerRule(new JenkinsRule())
            .around(new MongoDataLoadRule());

    @Test
    @LocalData
    @Ignore
    public void should_save_or_update_a_project() {
        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();

    }

    @Test
    @LocalData
    @Ignore
    public void should_find_a_child() {
        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();
    }

    @Test
    @LocalData
    @Ignore
    public void should_find_its_children() {
        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();
    }

    @Test
    @LocalData
    @Ignore
    public void should_delete_a_project() {
        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();
    }

    @Test
    @LocalData
    @Ignore
    public void should_find_project_for_a_url() {
        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();
    }

    @Test
    @LocalData
    @Ignore
    public void should_create_a_new_project() {
        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();
    }

    @Test
    @LocalData
    @Ignore
    public void should_check_if_a_project_exists() {
        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();
    }

    @Test
    @LocalData
    @Ignore
    public void should_find_a_project_by_id() {
        DynamicProjectRepository repo = SetupConfig.get().getDynamicProjectRepository();
    }
}
