package com.groupon.jenkins.mongo;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.github.services.GithubAccessTokenRepository;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.RuleChain;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

public class GithubAccessTokenRepositoryTest {
    @Rule
    public RuleChain chain = RuleChain
            .outerRule(new JenkinsRule())
            .around(new MongoDataLoadRule());

    @Test
    @LocalData
    public void should_find_access_tokens_by_url() {
        GithubAccessTokenRepository repo = SetupConfig.get().getGithubAccessTokenRepository();
        String accessToken = repo.getAccessToken("https://github.com/Groupon/DotCi");
        assertEquals("0123/0123456789abcdefghijklmnopqrstuvwxyz0123456789abcdefgh/0123456789abcdefghi/012345==", accessToken);
    }

    @Test
    @LocalData
    public void should_find_associated_login_by_url() {
        GithubAccessTokenRepository repo = SetupConfig.get().getGithubAccessTokenRepository();
        String username = repo.getAssociatedLogin("https://github.com/Groupon/DotCi");
        assertEquals("bestuser", username);
    }

    @Test
    @LocalData
    @Ignore
    public void should_update_an_access_token() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_check_if_a_url_is_configured() {

    }

    @Test
    @LocalData
    @Ignore
    public void should_add_an_access_token_based_on_a_url() {

    }
}
