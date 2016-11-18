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
package com.groupon.jenkins;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.groupon.jenkins.mongo.JenkinsMapper;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;

import javax.inject.Singleton;
import java.net.UnknownHostException;

public class DotCiModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    Mongo providesMongo() {
        Mongo mongo = null;

        try {
            final MongoClientOptions mongoClientOptions = MongoClientOptions.builder().autoConnectRetry(true).readPreference(ReadPreference.primaryPreferred()).build();
            mongo = new MongoClient(SetupConfig.get().getMongoServerAddresses(), mongoClientOptions);
        } catch (final UnknownHostException e) {
            addError(e);
        }

        return mongo;
    }

    @Provides
    @Singleton
    Datastore provideDatastore(final Mongo mongo) {
        final String databaseName = SetupConfig.get().getDbName();

        final Mapper mapper = new JenkinsMapper();
        final Morphia morphia = new Morphia(mapper);
        return morphia.createDatastore(mongo, databaseName);

    }
}
