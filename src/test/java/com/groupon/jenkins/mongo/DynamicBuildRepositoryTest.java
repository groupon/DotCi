package com.groupon.jenkins.mongo;

import com.groupon.jenkins.SetupConfig;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.RuleChain;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class DynamicBuildRepositoryTest {
    @Rule
    public RuleChain chain = RuleChain
            .outerRule(new JenkinsRule())
            .around(new MongoDataLoadRule());

    @Test
    @LocalData
    @Ignore
    public void should_save_a_build() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_find_the_builds_for_a_project() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_find_the_builds_for_a_project_as_a_map() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_find_the_first_build_for_a_project() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_find_the_last_build_for_a_project() {

    }


    @Test
    @LocalData
    @Ignore
    public void should_find_the_last_failed_build_for_a_project() {

    }


    @Test
    @LocalData
    @Ignore
    public void should_find_the_last_successful_build_for_a_project() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_find_the_last_successful_build_for_a_project_on_a_branch() {

    }


    @Test
    @LocalData
    @Ignore
    public void should_find_the_most_recent_n_builds_for_a_project() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_if_a_build_number_exists_for_a_project() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_get_a_build_by_number_for_a_project() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_get_a_build_by_sha_for_a_project() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_check_if_a_project_has_builds() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_count_how_many_builds_a_project_has() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_get_the_builds_after_a_number_for_a_project_on_a_branch() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_get_the_builds_after_a_number_for_a_project_for_a_user() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_get_the_lastest_builds_for_a_project_on_a_branch() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_get_the_builds_for_a_project_for_a_user() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_delete_a_build() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_delete_a_project() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_get_the_previous_build_for_a_project_on_a_branch() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_get_the_current_for_a_number_for_a_project() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_get_the_last_builds_for_a_project_for_a_user() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_get_the_last_build_for_a_project_on_a_branch() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_get_the_build_history_for_a_compute_node() {

    }
}
