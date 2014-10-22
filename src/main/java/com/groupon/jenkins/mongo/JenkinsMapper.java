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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.thoughtworks.xstream.XStream;
import hudson.util.CopyOnWriteList;
import jenkins.model.Jenkins;
import org.mongodb.morphia.mapping.CustomMapper;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MapperOptions;
import org.mongodb.morphia.mapping.cache.EntityCache;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class JenkinsMapper extends Mapper {
    private final Set<String> xmlClasses;
    private final XStream xmlProcessor;
    public static final String XML_FIELD_NAME = "xml";

    public JenkinsMapper() {
        super();
        setOptions(new JenkinsMongoOptions());
        getOptions().setActLikeSerializer(true);
        getOptions().objectFactory = new CustomMorphiaObjectFactory(Jenkins.getInstance().getPluginManager().uberClassLoader);

        xmlProcessor = Jenkins.XSTREAM2;
        xmlClasses = new HashSet<String>();

        includeSpecialConverters();
        includedSpecialMappedClasses();
    }

    @Override
    public DBObject toDBObject(final Object entity) {
        return toDBObject(entity, new LinkedHashMap<Object, DBObject>());
    }

    @Override
    public DBObject toDBObject(final Object entity, final Map<Object, DBObject> involvedObjects) {
        if( ! (entity instanceof CopyOnWriteList || involvedObjects.containsKey(entity) )) {
            involvedObjects.put(entity, null);
        }

        if(shouldUseXml(entity.getClass())) {
            return toXmlObject(entity);
        } else {
            return super.toDBObject(entity, involvedObjects);
        }
    }

    @Override
    public Object fromDBObject(final Class entityClass, final DBObject dbObject, final EntityCache cache) {
        if(shouldUseXml(entityClass)) {
            return fromXmlObject(dbObject);
        }
        return super.fromDBObject(entityClass, dbObject, cache);
    }

    private boolean shouldUseXml(Class clazz) {
        return false && xmlClasses.contains(clazz.getName());
    }

    private Object fromXmlObject(DBObject xmlObject) {
        String xml = (String) xmlObject.get(XML_FIELD_NAME);

        return xmlProcessor.fromXML(xml);
    }

    private DBObject toXmlObject(Object entity) {
        String xml = xmlProcessor.toXML(entity);

        DBObject dbObject = new BasicDBObject(XML_FIELD_NAME, xml);
        dbObject.put(CLASS_NAME_FIELDNAME, entity.getClass().getName());

        return dbObject;
    }

    /**
     * These classes have difficult types to discover using Morphia's libraries
     */
    protected void includedSpecialMappedClasses() {
        addMappedClass(new CopyOnWriteListMappedClass(this));

        // Should be saved as xml
        xmlClasses.add("hudson.security.AuthorizationMatrixProperty");
    }

    protected void includeSpecialConverters() {
        getConverters().addConverter(new ParametersDefinitionPropertyCoverter());
        getConverters().addConverter(new CombinationConverter());
        getConverters().addConverter(new AxisListConverter());
        getConverters().addConverter(new ResultConverter());
        getConverters().addConverter(new PermissionsConverter());
    }
}

class JenkinsMongoOptions extends MapperOptions {
    private final JenkinsEmbeddedMapper jenkinsEmbeddedMapper = new JenkinsEmbeddedMapper();

    @Override
    public CustomMapper getDefaultMapper() {
        return jenkinsEmbeddedMapper;
    }
}

