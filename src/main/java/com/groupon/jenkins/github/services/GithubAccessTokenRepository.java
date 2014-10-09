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
package com.groupon.jenkins.github.services;

import com.groupon.jenkins.mongo.MongoRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import hudson.util.Secret;
import java.io.IOException;
import org.acegisecurity.context.SecurityContextHolder;
import org.jenkinsci.plugins.GithubAuthenticationToken;
import org.kohsuke.github.GitHub;

public class GithubAccessTokenRepository extends MongoRepository {

    public static final String COLLECTION_NAME = "github_tokens";

    public GithubAccessTokenRepository() {
    }

    public String getAccessToken(String repourl) {
        DBObject token = getToken(repourl);
        return Secret.fromString(token.get("access_token").toString()).getPlainText();
    }

    public String getAssociatedLogin(String repoUrl) {
        DBObject token = getToken(repoUrl);
        return token.get("user").toString();
    }

    private DBObject getToken(String repourl) {
        BasicDBObject query = new BasicDBObject("repo_url", repourl);
        return getCollection().findOne(query);
    }

      public void put(String url) throws IOException {
        DBObject token = getToken(url);

        GithubAuthenticationToken auth = getAuthentication();
        GitHub gh = auth.getGitHub();
        String accessToken = getEncryptedToken(auth);

            if (token != null) {
                 getCollection().remove(token);
            }

            BasicDBObject doc = new BasicDBObject("user", gh.getMyself().getLogin()).append("access_token", accessToken).append("repo_url", url);
            getCollection().insert(doc);
      }

    private GithubAuthenticationToken getAuthentication() {
        return (GithubAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }

    private String getEncryptedToken(GithubAuthenticationToken auth) {
        return Secret.fromString(auth.getAccessToken()).getEncryptedValue();
    }

    public void updateAccessToken(String username) {
        BasicDBObject query = new BasicDBObject("user", username);
        BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("access_token", getEncryptedToken(getAuthentication())));
        getCollection().update(query,update,false,true);
    }

    public boolean isConfigured(String url) {
        return getToken(url) !=null;
    }

    protected DBCollection getCollection() {
        return getDatastore().getDB().getCollection(COLLECTION_NAME);
    }
}
