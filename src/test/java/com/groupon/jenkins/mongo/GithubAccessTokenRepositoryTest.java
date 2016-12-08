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
import com.groupon.jenkins.github.services.GithubAccessTokenRepository;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import static org.junit.Assert.assertEquals;

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
