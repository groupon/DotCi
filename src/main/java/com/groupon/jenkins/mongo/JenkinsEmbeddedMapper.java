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
import com.thoughtworks.xstream.converters.reflection.SerializationMethodInvoker;
import hudson.model.CauseAction;
import hudson.security.Permission;
import hudson.util.CopyOnWriteList;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.mapping.CustomMapper;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MappingException;
import org.mongodb.morphia.mapping.cache.EntityCache;
import org.mongodb.morphia.utils.IterHelper;
import org.mongodb.morphia.utils.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class JenkinsEmbeddedMapper implements CustomMapper {
    private final Map<Class, CustomMapper> customMappers;

    private final MapKeyValueMapper awkwardMapper;

    private final SerializationMethodInvoker serializationMethodInvoker;

    JenkinsEmbeddedMapper() {
        this.customMappers = new HashMap<>();
        this.customMappers.put(CopyOnWriteList.class, new CopyOnWriteListMapper());

        this.serializationMethodInvoker = new SerializationMethodInvoker();

        this.awkwardMapper = new MapKeyValueMapper();
    }

    private Class getClass(final DBObject dbObj) {
        // see if there is a className value
        final String className = (String) dbObj.get(Mapper.CLASS_NAME_FIELDNAME);
        if (className != null && className.equals(CauseAction.class.getName())) {
            // try to Class.forName(className) as defined in the dbObject first,
            // otherwise return the entityClass
            try {
                return Class.forName(className, true, getClassLoaderForClass(className, dbObj));
            } catch (final ClassNotFoundException e) {
                //Ignore if notfound
            }
        }
        return null;
    }

    protected ClassLoader getClassLoaderForClass(final String clazz, final DBObject object) {
        return Thread.currentThread().getContextClassLoader();
    }

    @Override
    public void toDBObject(final Object entity, final MappedField mf, final DBObject dbObject, final Map<Object, DBObject> involvedObjects, final Mapper mapper) {
        final Object fieldValue = mf.getFieldValue(entity);
        if (fieldValue == null) return; // nothing to do


        if (mapper.getConverters().hasSimpleValueConverter(mf) || mapper.getConverters().hasSimpleValueConverter(fieldValue)) {
            mapper.getOptions().getValueMapper().toDBObject(entity, mf, dbObject, involvedObjects, mapper);
        } else if (this.customMappers.containsKey(fieldValue.getClass())) {
            this.customMappers.get(fieldValue.getClass()).toDBObject(entity, mf, dbObject, involvedObjects, mapper);
        } else if (isAwkwardMap(mf)) {
            this.awkwardMapper.toDBObject(entity, mf, dbObject, involvedObjects, mapper);
        } else {
            // Genericly handle an object for serialization

            if (involvedObjects.containsKey(fieldValue)) { // already once serialized
                DBObject cachedStub = involvedObjects.get(fieldValue);

                if (cachedStub == null) {  // visited, but not yet serialized, usually from mapper.toDBObject(...)
                    final DBObject newStub = createStub(mapper, fieldValue, mapper.getId(fieldValue));
                    involvedObjects.put(fieldValue, newStub);
                    cachedStub = newStub;
                } else if (isFullySerializedObject(cachedStub, mapper)) {
                    // remove full object from the cache and replace it with an ObjectId reference
                    // so we're not storing redundant data
                    final Object id = cachedStub.get(mapper.ID_KEY);
                    final DBObject newStub = createStub(mapper, fieldValue, id);

                    involvedObjects.put(fieldValue, newStub);
                    cachedStub = newStub;
                }

                dbObject.put(mf.getNameToStore(), cachedStub); // serialize as the reference

            } else {
                mapper.getOptions().getEmbeddedMapper().toDBObject(entity, mf, dbObject, involvedObjects, mapper);
            }

        }
    }

    private boolean isFullySerializedObject(final DBObject cachedStub, final Mapper mapper) {
        return cachedStub.keySet().size() > 2
            && cachedStub.containsField(mapper.ID_KEY)
            && cachedStub.containsField(mapper.CLASS_NAME_FIELDNAME);
    }

    private boolean isAwkwardMap(final MappedField mf) {
        return mf.isMap() && Permission.class == mf.getMapKeyClass();
    }

    private DBObject createStub(final Mapper mapper, final Object fieldValue, Object id) {
        if (id == null) {
            id = new ObjectId();
        }

        final DBObject stub = new BasicDBObject(mapper.CLASS_NAME_FIELDNAME, fieldValue.getClass().getName());
        stub.put(mapper.ID_KEY, id);

        return stub;
    }

    private Key extractKey(final Mapper mapper, final MappedField mf, final DBObject dbObject) {
        if (mapper == null || mf == null || dbObject == null || (dbObject instanceof BasicDBList))
            return null;

        final ObjectId objectId = (ObjectId) dbObject.get(mapper.ID_KEY);
        //HACKY GET RID OF SOON
        final Object obj = mapper.getOptions().getObjectFactory().createInstance(mapper, mf, dbObject);

        if (objectId == null || obj == null) return null;

        return new Key(obj.getClass(), objectId);
    }

    @Override
    public void fromDBObject(final DBObject dbObject, final MappedField mf, final Object entity, final EntityCache cache, final Mapper mapper) {

        final Key key = extractKey(mapper, mf, (DBObject) dbObject.get(mf.getNameToStore()));
        if (key != null && cache.getEntity(key) != null) {
            final Object object = cache.getEntity(key);
            mf.setFieldValue(entity, object);
        } else if (this.customMappers.containsKey(mf.getType())) {
            this.customMappers.get(mf.getType()).fromDBObject(dbObject, mf, entity, cache, mapper);
        } else if (isAwkwardMap(mf)) {
            this.awkwardMapper.fromDBObject(dbObject, mf, entity, cache, mapper);
        } else {
            // the first two conditions (isMap and isMultipleValues) are part of the hack to fix handling primitives
            if (mf.isMap()) {
                readMap(dbObject, mf, entity, cache, mapper);
            } else if (mf.isMultipleValues()) {
                readCollection(dbObject, mf, entity, cache, mapper);
            } else {
                mapper.getOptions().getEmbeddedMapper().fromDBObject(dbObject, mf, entity, cache, mapper);
            }

            this.serializationMethodInvoker.callReadResolve(mf.getFieldValue(entity));
        }


    }

    // Taken from Morphia's Embedded Mapper and is almost identical to that method
    @Deprecated
    private void readMap(final DBObject dbObject, final MappedField mf, final Object entity, final EntityCache cache, final Mapper mapper) {
        final Map map = mapper.getOptions().getObjectFactory().createMap(mf);

        final DBObject dbObj = (DBObject) mf.getDbObjectValue(dbObject);
        new IterHelper<>().loopMap(dbObj, new IterHelper.MapIterCallback<Object, Object>() {
            @Override
            public void eval(final Object key, final Object val) {
                Object newEntity = null;

                //run converters
                if (val != null) {
                    if (mapper.getConverters().hasSimpleValueConverter(mf) || mapper.getConverters()
                        .hasSimpleValueConverter(mf.getSubClass())) {
                        newEntity = mapper.getConverters().decode(mf.getSubClass(), val, mf);
                    } else if (mapper.getConverters().hasSimpleValueConverter(val.getClass())) { // added this condition to handle incorrectly mapped primitives.
                        newEntity = mapper.getConverters().decode(val.getClass(), val, mf);
                    } else {
                        if (val instanceof DBObject) {
                            newEntity = readMapOrCollectionOrEntity((DBObject) val, mf, cache, mapper);
                        } else {
                            throw new MappingException("Embedded element isn't a DBObject! How can it be that is a " + val.getClass());
                        }

                    }
                }

                final Object objKey = mapper.getConverters().decode(mf.getMapKeyClass(), key);
                map.put(objKey, newEntity);
            }
        });

        if (!map.isEmpty()) {
            mf.setFieldValue(entity, map);
        }
    }


    // Taken from Morphia's Embedded Mapper
    @Deprecated
    private Object readMapOrCollectionOrEntity(final DBObject dbObj, final MappedField mf, final EntityCache cache, final Mapper mapper) {
        try {

            // to get around the protected access
            final Method method = Mapper.class.getDeclaredMethod("fromDb", DBObject.class, Object.class, EntityCache.class);
            method.setAccessible(true);
            if (Map.class.isAssignableFrom(mf.getSubClass()) || Iterable.class.isAssignableFrom(mf.getSubClass())) {
                final MapOrCollectionMF mocMF = new MapOrCollectionMF((ParameterizedType) mf.getSubType());

                method.invoke(mapper, dbObj, mocMF, cache);

                return mocMF.getValue();
            } else {
                final Object newEntity = mapper.getOptions().getObjectFactory().createInstance(mapper, mf, dbObj);
                return method.invoke(mapper, dbObj, newEntity, cache);
            }
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException("Mapper class does not have fromDb method", e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException("Unable to call fromDb on Mapper with arguments", e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Security model prevents calling fromDb on Mapper", e);
        }
    }

    // Taken from Morphia's Embedded Mapper and is almost identical to that method
    @Deprecated
    private void readCollection(final DBObject dbObject, final MappedField mf, final Object entity, final EntityCache cache,
                                final Mapper mapper) {
        // multiple documents in a List
        final Collection values = mf.isSet() ? mapper.getOptions().getObjectFactory().createSet(mf)
            : mapper.getOptions().getObjectFactory().createList(mf);

        final Object dbVal = mf.getDbObjectValue(dbObject);
        if (dbVal != null) {

            final List dbValues;
            if (dbVal instanceof List) {
                dbValues = (List) dbVal;
            } else {
                dbValues = new BasicDBList();
                dbValues.add(dbVal);
            }

            for (final Object o : dbValues) {

                Object newEntity = null;

                if (o != null) {
                    //run converters
                    if (mapper.getConverters().hasSimpleValueConverter(mf) || mapper.getConverters()
                        .hasSimpleValueConverter(mf.getSubClass())) {
                        newEntity = mapper.getConverters().decode(mf.getSubClass(), o, mf);
                    } else if (mapper.getConverters().hasSimpleValueConverter(o.getClass())) {// added this condition to handle incorrectly mapped primitives.
                        newEntity = mapper.getConverters().decode(o.getClass(), o, mf);
                    } else {
                        if (o instanceof DBObject) {
                            final Class clazz = getClass((DBObject) o);
                            if (clazz != null && mapper.getConverters().hasSimpleValueConverter(clazz)) {
                                newEntity = mapper.getConverters().decode(clazz, o, mf);
                            } else {
                                newEntity = readMapOrCollectionOrEntity((DBObject) o, mf, cache, mapper);
                            }
                        } else {
                            throw new MappingException("Embedded element isn't a DBObject! How can it be that is a " + o.getClass());
                        }
                    }
                }

                values.add(newEntity);
            }
        }
        if (!values.isEmpty() || mapper.getOptions().isStoreEmpties()) {
            if (mf.getType().isArray()) {
                mf.setFieldValue(entity, ReflectionUtils.convertToArray(mf.getSubClass(), ReflectionUtils.iterToList(values)));
            } else {
                mf.setFieldValue(entity, values);
            }
        }
    }

}

// Taken from Morphia's Embedded Mapper
@Deprecated
class MapOrCollectionMF extends MappedField {
    private ParameterizedType pType;
    private Object value;

    MapOrCollectionMF() {
        this.isSingleValue = false;
    }

    MapOrCollectionMF(final ParameterizedType t) {
        this();
        this.pType = t;
        final Class rawClass = (Class) t.getRawType();
        this.isSet = ReflectionUtils.implementsInterface(rawClass, Set.class);
        this.isMap = ReflectionUtils.implementsInterface(rawClass, Map.class);
        this.mapKeyType = getMapKeyClass();
        this.subType = getSubType();
        this.isMongoType = ReflectionUtils.isPropertyType(getSubClass());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        final MapOrCollectionMF other = new MapOrCollectionMF();
        other.pType = this.pType;
        other.isSet = this.isSet;
        other.isMap = this.isMap;
        other.mapKeyType = this.mapKeyType;
        other.subType = this.subType;
        other.isMongoType = this.isMongoType;
        return other;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public String getNameToStore() {
        return "superFake";
    }

    @Override
    public Object getDbObjectValue(final DBObject dbObj) {
        return dbObj;
    }

    @Override
    public boolean hasAnnotation(final Class ann) {
        return Embedded.class.equals(ann);
    }

    @Override
    public String toString() {
        return "MapOrCollectionMF for " + super.toString();
    }

    @Override
    public Class getType() {
        return this.isMap ? Map.class : List.class;
    }

    @Override
    public Class getMapKeyClass() {
        return (Class) (isMap() ? this.pType.getActualTypeArguments()[0] : null);
    }

    @Override
    public Type getSubType() {
        return this.pType.getActualTypeArguments()[isMap() ? 1 : 0];
    }

    @Override
    public Class getSubClass() {
        return toClass(getSubType());
    }

    @Override
    public boolean isSingleValue() {
        return false;
    }

    @Override
    public Object getFieldValue(final Object classInst) {
        return this.value;
    }

    @Override
    public void setFieldValue(final Object classInst, final Object val) {
        this.value = val;
    }
}
