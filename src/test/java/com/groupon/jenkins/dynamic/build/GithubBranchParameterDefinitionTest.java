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

package com.groupon.jenkins.dynamic.build;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRepository;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubBranchParameterDefinitionTest {

    private GithubBranchParameterDefinition githubBranchParameterDefinition;
    private GHRepository githubRepo;

    @Before
    public void setup() {
        this.githubBranchParameterDefinition = Mockito.spy(new GithubBranchParameterDefinition("name", "defaultVaulue", "http://github.url"));
        this.githubRepo = Mockito.mock(GHRepository.class);
        Mockito.doReturn(this.githubRepo).when(this.githubBranchParameterDefinition).getGhRepository();
    }

    @Test
    public void should_suggest_branches() throws Exception {
        when(this.githubRepo.getBranches()).thenReturn(ImmutableMap.<String, GHBranch>of("branch1", mock(GHBranch.class)));
        final Iterable<String> branches = this.githubBranchParameterDefinition.getBranches();
        Assert.assertEquals("branch1", Iterables.get(branches, 0));
    }

    @Test
    public void should_suggest_prs() throws Exception {

        final Iterable<String> branches = this.githubBranchParameterDefinition.getBranches();
        Assert.assertEquals("Pull Request: ", Iterables.get(branches, 0));
    }

}
