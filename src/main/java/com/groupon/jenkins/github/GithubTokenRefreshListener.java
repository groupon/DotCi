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

package com.groupon.jenkins.github;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.github.services.GithubAccessTokenRepository;
import hudson.Extension;
import jenkins.security.SecurityListener;
import org.acegisecurity.userdetails.UserDetails;

import javax.annotation.Nonnull;

@Extension
public class GithubTokenRefreshListener extends SecurityListener {
    @Override
    protected void authenticated(@Nonnull UserDetails details) {
        SetupConfig.get().getGithubAccessTokenRepository().updateAccessToken(details.getUsername());
    }

    @Override
    protected void failedToAuthenticate(@Nonnull String username) {

    }

    @Override
    protected void loggedIn(@Nonnull String username) {

    }

    @Override
    protected void failedToLogIn(@Nonnull String username) {

    }

    @Override
    protected void loggedOut(@Nonnull String username) {

    }
}
