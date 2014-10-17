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

import com.groupon.jenkins.dynamic.build.DbBackedProject;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoOptions;
import hudson.matrix.AxisList;
import hudson.model.ParametersDefinitionProperty;
import hudson.util.CopyOnWriteList;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import org.mongodb.morphia.mapping.CustomMapper;
import org.mongodb.morphia.mapping.MappedClass;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MapperOptions;
import org.mongodb.morphia.mapping.cache.EntityCache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JenkinsMapper extends Mapper {

    public JenkinsMapper() {
        super();
        setOptions(new JenkinsMongoOptions());
        getOptions().setActLikeSerializer(true);
        getOptions().objectFactory = new CustomMorphiaObjectFactory(Jenkins.getInstance().getPluginManager().uberClassLoader);

        includeSpecialConverters();
        includedSpecialMappedClasses();
    }

    @Override
    public DBObject toDBObject(final Object entity) {
        return toDBObject(entity, new HashMap<Object,DBObject>());
    }

    @Override
    public DBObject toDBObject(final Object entity, final Map<Object, DBObject> involvedObjects) {
        involvedObjects.put(entity, null);
        return super.toDBObject(entity, involvedObjects);
    }

    /**
     * These classes have difficult types to discover using Morphia's libraries
     */
    protected void includedSpecialMappedClasses() {
        addMappedClass(new CopyOnWriteListMappedClass(this));
    }

    protected void includeSpecialConverters() {
        getConverters().addConverter(new ParametersDefinitionPropertyCoverter());
        getConverters().addConverter(new CombinationConverter());
        getConverters().addConverter(new AxisListConverter());
        getConverters().addConverter(new ResultConverter());
    }
}

class JenkinsMongoOptions extends MapperOptions {
    private final JenkinsEmbeddedMapper jenkinsEmbeddedMapper = new JenkinsEmbeddedMapper();

    @Override
    public CustomMapper getDefaultMapper() {
        return jenkinsEmbeddedMapper;
    }
}

