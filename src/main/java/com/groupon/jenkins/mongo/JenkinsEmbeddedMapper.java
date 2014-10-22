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

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import hudson.security.Permission;
import hudson.util.CopyOnWriteList;
import jenkins.model.Jenkins;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.mapping.CustomMapper;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.EntityCache;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class JenkinsEmbeddedMapper implements CustomMapper {
    private final Map<Class, CustomMapper> customMappers;
    private final Set<String> xmlClasses;
    private final XmlMapper xmlMapper;
    private final AwkwardMapMapper awkwardMapper;

    JenkinsEmbeddedMapper() {
        customMappers = new HashMap<Class, CustomMapper>();
        customMappers.put(CopyOnWriteList.class, new CopyOnWriteListMapper());

        xmlMapper = new XmlMapper(Jenkins.XSTREAM2);
        awkwardMapper = new AwkwardMapMapper();

        xmlClasses = new HashSet<String>();
        xmlClasses.add("hudson.security.AuthorizationMatrixProperty");
    }

    @Override
    public void toDBObject(Object entity, MappedField mf, DBObject dbObject, Map<Object, DBObject> involvedObjects, Mapper mapper) {
        Object fieldValue = mf.getFieldValue(entity);
        if(fieldValue == null) return; // nothing to do

        if(customMappers.containsKey(fieldValue.getClass())) {
            customMappers.get(fieldValue.getClass()).toDBObject(entity, mf, dbObject, involvedObjects, mapper);
        } else if (isAwkwardMap(mf)) {
            awkwardMapper.toDBObject(entity, mf, dbObject, involvedObjects, mapper);
        } else {
            if(involvedObjects.containsKey(fieldValue)) {
                DBObject cachedStub = involvedObjects.get(fieldValue);

                if(cachedStub == null) {
                    DBObject newStub = createStub(mapper, fieldValue, mapper.getId(fieldValue));
                    involvedObjects.put(fieldValue, newStub);
                    cachedStub = newStub;
                } else if (cachedStub.keySet().size() > 2
                        && cachedStub.containsField(mapper.ID_KEY)
                        && cachedStub.containsField(mapper.CLASS_NAME_FIELDNAME)) {

                    // remove full object from the cache and replace it with the stub
                    Object id = cachedStub.get(mapper.ID_KEY);
                    DBObject newStub = createStub(mapper, fieldValue, id);

                    involvedObjects.put(fieldValue, newStub);
                    cachedStub = newStub;
                }

                dbObject.put(mf.getNameToStore(), cachedStub);

            } else {
                mapper.getOptions().getEmbeddedMapper().toDBObject(entity, mf, dbObject, involvedObjects, mapper);
            }

        }
    }

    private boolean isAwkwardMap(MappedField mf) {
        return mf.isMap() && Permission.class == mf.getMapKeyClass();
    }

    private DBObject createStub(Mapper mapper, Object fieldValue, Object id) {
        if(id == null) {
            id = new ObjectId();
        }

        DBObject stub = new BasicDBObject(mapper.CLASS_NAME_FIELDNAME, fieldValue.getClass().getName());
        stub.put(mapper.ID_KEY, id);

        return stub;
    }

    private Key extractKey(Mapper mapper, MappedField mf, DBObject dbObject) {
        if(mapper == null || mf == null || dbObject == null ||  (dbObject instanceof BasicDBList)) return null;

        ObjectId objectId = (ObjectId) dbObject.get(mapper.ID_KEY);
        //HACKY GET RID OF SOON
        Object obj = mapper.getOptions().getObjectFactory().createInstance(mapper, mf, dbObject);

        if(objectId == null || obj == null) return null;

        return new Key(obj.getClass(), objectId);
    }

    @Override
    public void fromDBObject(DBObject dbObject, MappedField mf, Object entity, EntityCache cache, Mapper mapper) {

        Key key = extractKey(mapper, mf, (DBObject) dbObject.get(mf.getNameToStore()));
        if(key != null && cache.getEntity(key) != null) {
            Object object = cache.getEntity(key);
            mf.setFieldValue(entity, object);
        } else if(customMappers.containsKey(mf.getType())) {
            customMappers.get(mf.getType()).fromDBObject(dbObject, mf, entity, cache, mapper);
        } else if (isAwkwardMap(mf)) {
            awkwardMapper.fromDBObject(dbObject, mf, entity, cache, mapper);
        } else {
            mapper.getOptions().getEmbeddedMapper().fromDBObject(dbObject, mf, entity, cache, mapper);
        }
    }
}

