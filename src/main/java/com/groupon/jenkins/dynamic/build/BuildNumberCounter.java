package com.groupon.jenkins.dynamic.build;

import org.mongodb.morphia.annotations.Entity;

@Entity("build_number_counter")
public class BuildNumberCounter {
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
