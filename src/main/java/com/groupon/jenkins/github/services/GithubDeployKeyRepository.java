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

import com.groupon.jenkins.github.DeployKeyPair;
import com.groupon.jenkins.mongo.MongoRepository;
import com.groupon.jenkins.util.EncryptionService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.io.IOException;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;

public class GithubDeployKeyRepository extends MongoRepository {
    private EncryptionService encryptionService;

    @Inject
    public GithubDeployKeyRepository(Datastore datastore) {
        super(datastore);
        this.encryptionService = new EncryptionService();
    }


    public void put(String url, DeployKeyPair keyPair) throws IOException {
        BasicDBObject query = new BasicDBObject("repo_url", url);
        BasicDBObject doc = new BasicDBObject("public_key", encrypt(keyPair.publicKey)).append("private_key",encrypt(keyPair.privateKey)).append("repo_url", url);
        getCollection().update(query, doc, true, false);
    }
    protected DBCollection getCollection() {
        return getDatastore().getDB().getCollection("deploy_keys");
    }

    public DeployKeyPair get(String repoUrl) {
        BasicDBObject query = new BasicDBObject("repo_url", repoUrl);
        DBObject keyPair = getCollection().findOne(query);
        return new DeployKeyPair(decrypt((String)keyPair.get("public_key")),decrypt( (String)keyPair.get("private_key")));
    }

    public boolean hasDeployKey(String repoUrl) {
        BasicDBObject query = new BasicDBObject("repo_url", repoUrl);
        return getCollection().getCount(query) > 0;
    }
    protected String encrypt(String value) {
        return  encryptionService.encrypt(value);
    }

    private  String decrypt(String value){
        return encryptionService.decrypt(value);
    }
}
