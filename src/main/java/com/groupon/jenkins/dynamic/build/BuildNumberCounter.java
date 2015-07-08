package com.groupon.jenkins.dynamic.build;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("build_number_counter")
public class BuildNumberCounter {
    @Id
    private ObjectId id = new ObjectId();

    private String key;
    private int counter;

    public BuildNumberCounter(String key, int counter) {
        this.key = key;
        this.counter = counter;
    }

    public int getCounter() {
        return counter;
    }
}
