/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.groupon.jenkins.github.services;

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.SetupConfig;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GithubRepositoryServiceTest {

    private GHRepository githubRepository;
    private SetupConfig setupConfig;
    private GithubAccessTokenRepository githubAccessTokenRepository;
    private GithubRepositoryService githubRepositoryService;
    private GithubDeployKeyRepository githubDeployKeyRepository;

    @Before
    public void setup() throws MalformedURLException {
        githubRepository = mock(GHRepository.class);
        when(githubRepository.getHtmlUrl()).thenReturn(new URL("http://github.com/meow"));
        setupConfig = mock(SetupConfig.class);
        githubAccessTokenRepository = mock(GithubAccessTokenRepository.class);
        githubDeployKeyRepository = mock(GithubDeployKeyRepository.class);
        githubRepositoryService = spy(new GithubRepositoryService(githubRepository, githubAccessTokenRepository, githubDeployKeyRepository));
        doReturn(setupConfig).when(githubRepositoryService).getSetupConfig();
    }

    @Test
    public void should_add_hook_with_the_url_from_dotci_configuration() throws IOException {
        when(setupConfig.getGithubCallbackUrl()).thenReturn("http://jenkins/githook/");

        githubRepositoryService.addHook("access_token", "user");

        List<GHEvent> events = Arrays.asList(GHEvent.PUSH, GHEvent.PULL_REQUEST);
        verify(githubRepository).createHook("web", ImmutableMap.of("url", "http://jenkins/githook/"), events, true);
    }

    @Test
    public void should_save_access_tokens_to_database() throws IOException {
        when(setupConfig.getGithubCallbackUrl()).thenReturn("http://jenkins/githook/");
        when(githubRepository.getHtmlUrl()).thenReturn(new URL("http://github.com/kittah/crunchies"));

        githubRepositoryService.addHook("access_token", "user");

        verify(githubAccessTokenRepository).put("http://github.com/kittah/crunchies", "access_token", "user");
    }

    @Test
    public void should_add_traling_slash_if_missing() throws IOException {
        when(setupConfig.getGithubCallbackUrl()).thenReturn("http://jenkins/githook");

        githubRepositoryService.addHook("access_token", null);

        List<GHEvent> events = Arrays.asList(GHEvent.PUSH, GHEvent.PULL_REQUEST);
        verify(githubRepository).createHook("web", ImmutableMap.of("url", "http://jenkins/githook/"), events, true);
    }


//    @Test //TODO: uncomment this when encryption service branch is merged in
//    public void should_delete_existing_deploy_key_before_adding_new_one() throws IOException {
//        when(githubRepository.isPrivate()).thenReturn(true);
//        GHDeployKey deployKey = mock(GHDeployKey.class);
//        when(deployKey.getTitle()).thenReturn("DotCi");
//        when(githubRepository.getDeployKeys()).thenReturn(Arrays.asList(deployKey));
//
//        githubRepositoryService.addDeployKey();
//
//        verify(deployKey).delete();
//
//    }
}
