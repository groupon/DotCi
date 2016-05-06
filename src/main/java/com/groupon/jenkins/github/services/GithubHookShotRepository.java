package com.groupon.jenkins.github.services;

import com.google.inject.Inject;
import com.groupon.jenkins.mongo.MongoRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.mongodb.morphia.Datastore;

import java.util.Date;

public class GithubHookShotRepository extends MongoRepository {
    public static final String COLLECTION_NAME = "github_hookshots";
    @Inject
    public GithubHookShotRepository(Datastore datastore) {
        super(datastore);
    }
    protected DBCollection getCollection() {
        return getDatastore().getDB().getCollection(COLLECTION_NAME);
    }

    public  void saveHookShot(String payload){
        BasicDBObject doc = new BasicDBObject("date", new Date()).append("payload", payload);
        getCollection().insert(doc);
    }

    public String getNextHookShot() {
        DBObject hookShot = getCollection().findOne();
        if(hookShot != null){
            String payload = (String) hookShot.get("payload");
            getCollection().remove(hookShot);
            return  payload;
        }
        return null;
    }
}
