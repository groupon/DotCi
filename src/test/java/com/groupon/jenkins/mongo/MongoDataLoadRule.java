package com.groupon.jenkins.mongo;

import com.groupon.jenkins.SetupConfig;
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
    public Statement apply(final Statement base, final Description description){
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if(Jenkins.getInstance() == null
                        || SetupConfig.get().getInjector() == null
                        || SetupConfig.get().getInjector().getInstance(Datastore.class) == null ) {
                    throw new IllegalStateException("Requires configured Jenkins and Mongo configurations");
                }

                DB db = SetupConfig.get().getInjector().getInstance(Datastore.class).getDB();

                //Load mongo data
                File homedir = Jenkins.getInstance().getRootDir();

                for(File fileOfData : homedir.listFiles()) {
                    if(! fileOfData.getName().endsWith(".json") ) continue;

                    String collectionName = fileOfData.getName().replaceAll("\\.json$", "");
                    DBCollection collection = db.createCollection(collectionName, new BasicDBObject());

                    String data = FileUtils.readFileToString(fileOfData);
                    Object bsonObject = JSON.parse(data);
                    if(bsonObject instanceof BasicDBList) {
                        BasicDBList basicDBList = (BasicDBList) bsonObject;
                        collection.insert(basicDBList.toArray(new DBObject[0]));
                    } else {
                        collection.insert((DBObject) bsonObject);
                    }

                }

                base.evaluate();

                // Clean up mongo data
                for(String collectioName : db.getCollectionNames()) {
                    db.getCollection(collectioName).drop();
                }
            }
        };
    }
}
