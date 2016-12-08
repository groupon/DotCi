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
import com.groupon.jenkins.dynamic.organizationcontainer.OrganizationContainer;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mongodb.morphia.Datastore;

import java.io.File;

public class MongoDataLoadRule implements TestRule {
    public MongoDataLoadRule() {
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (Jenkins.getInstance() == null
                    || SetupConfig.get().getInjector() == null
                    || SetupConfig.get().getInjector().getInstance(Datastore.class) == null) {
                    throw new IllegalStateException("Requires configured Jenkins and Mongo configurations");
                }

                DB db = SetupConfig.get().getInjector().getInstance(Datastore.class).getDB();

                //Load mongo data
                File homedir = Jenkins.getInstance().getRootDir();

                for (File fileOfData : homedir.listFiles()) {
                    if (!fileOfData.getName().endsWith(".json")) continue;

                    String collectionName = fileOfData.getName().replaceAll("\\.json$", "");
                    DBCollection collection = db.createCollection(collectionName, new BasicDBObject());

                    String data = FileUtils.readFileToString(fileOfData);
                    Object bsonObject = JSON.parse(data);
                    if (bsonObject instanceof BasicDBList) {
                        BasicDBList basicDBList = (BasicDBList) bsonObject;
                        collection.insert(basicDBList.toArray(new DBObject[0]));
                    } else {
                        collection.insert((DBObject) bsonObject);
                    }

                }

                for (OrganizationContainer container : Jenkins.getInstance().getAllItems(OrganizationContainer.class)) {
                    container.reloadItems();
                }

                base.evaluate();

                // Clean up mongo data
                for (String collectioName : db.getCollectionNames()) {
                    db.getCollection(collectioName).drop();
                }
            }
        };
    }
}
