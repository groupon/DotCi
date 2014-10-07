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
import com.mongodb.*;

import java.net.UnknownHostException;

import jenkins.model.Jenkins;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;

public abstract class MongoRepository {
    private static final transient Object lock = new Object();
    private static Datastore datastore;

    public Datastore getDatastore() {
        if(datastore == null) {
            synchronized (lock) {
                if(datastore == null) { // confirm we got the lock in time
                    datastore = configureDatastore();
                }
            }
        }

        return datastore;
    }

    private Datastore configureDatastore() {
        Morphia morphia = new Morphia();
        Mapper mapper = morphia.getMapper();
        mapper.getConverters().addConverter(new CopyOnWriteListConverter());
        mapper.getConverters().addConverter(new DescribableListConverter());
        mapper.getConverters().addConverter(new ParametersDefinitionPropertyCoverter());
        mapper.getConverters().addConverter(new CombinationConverter());
        mapper.getConverters().addConverter(new AxisListConverter());
        mapper.getConverters().addConverter(new ResultConverter());


        mapper.getOptions().setActLikeSerializer(true);
        mapper.getOptions().objectFactory = new CustomMorphiaObjectFactory(Jenkins.getInstance().getPluginManager().uberClassLoader);

        Mongo mongo;

        try {
            mongo = new MongoClient(SetupConfig.get().getDbHost(), SetupConfig.get().getDbPort());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        String databaseName = SetupConfig.get().getDbName();

        return morphia.createDatastore(mongo, databaseName);
    }

}
