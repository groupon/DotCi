package com.groupon.jenkins.dynamic.build;

import hudson.util.DelegatingOutputStream;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.UpdateOperations;

import java.io.IOException;
import java.io.OutputStream;

public class BuildLogOutputStream extends OutputStream{

    private BuildLog buildLog;
    private Datastore datastore;

    public BuildLogOutputStream(BuildLog buildLog, Datastore datastore) {
        this.buildLog = buildLog;
        this.datastore = datastore;
    }

    @Override
    public void write(int b) throws IOException {
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {

        buildLog.append(b,off,len);
        datastore.save(buildLog);
    }


}
