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
import org.mongodb.morphia.mapping.CustomMapper;
import org.mongodb.morphia.mapping.MappedClass;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.EntityCache;
import org.mongodb.morphia.utils.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

class AwkwardMapMapper implements CustomMapper {
    public static final String KEY = "key";
    public static final String VALUE = "value";

    @Override
    public void toDBObject(Object entity, MappedField mf, DBObject dbObject, Map<Object, DBObject> involvedObjects, Mapper mapper) {
        final String name = mf.getNameToStore();

        Map<?, ?> awkwardMap = (Map<?, ?>) mf.getFieldValue(entity);
        List out = new ArrayList();

        for(final Map.Entry entry : awkwardMap.entrySet()) {
            DBObject mappedEntry = new BasicDBObject();

            try {
                Field key = entry.getClass().getDeclaredField(KEY);
                EntryMappedField keyField = new EntryMappedField(key, entry.getClass(), mf);

                Field value = entry.getClass().getDeclaredField(VALUE);
                EntryMappedField  valueField = new EntryMappedField(value, entry.getClass(), mf);

                mapper.getOptions().getDefaultMapper().toDBObject(entry, keyField, mappedEntry, involvedObjects, mapper);
                mapper.getOptions().getDefaultMapper().toDBObject(entry, valueField, mappedEntry, involvedObjects, mapper);

                out.add(mappedEntry);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        dbObject.put(name, out);
    }

    @Override
    public void fromDBObject(DBObject dbObject, MappedField mf, Object entity, EntityCache cache, Mapper mapper) {
        BasicDBList entriesList = (BasicDBList) dbObject.get(mf.getNameToStore());

        final Map map = mapper.getOptions().getObjectFactory().createMap(mf);

        for(Object obj : entriesList) {
            DBObject listEntryDbObj = (DBObject) obj;
            //Map.Entry entry = (Map.Entry) mapper.getOptions().getObjectFactory().createInstance(Map.Entry.class);
            Pair entry = new Pair();
            try {
                Field key = entry.getClass().getDeclaredField(KEY);
                EntryMappedField keyField = new EntryMappedField(key, entry.getClass(), mf);

                Field value = entry.getClass().getDeclaredField(VALUE);
                EntryMappedField valueField = new EntryMappedField(value, entry.getClass(), mf);

                mapper.getOptions().getEmbeddedMapper().fromDBObject(listEntryDbObj, keyField, entry, cache, mapper);
                mapper.getOptions().getEmbeddedMapper().fromDBObject(listEntryDbObj, valueField, entry, cache, mapper);

                map.put(entry.getKey(), entry.getValue());

            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        mf.setFieldValue(entity, map);
    }
}

/**
 * Remove as soon as we're able to figure out how to instantiate Map.Entry.
 */
@Deprecated
class Pair {
    private Object key;
    private Object value;
    Pair() {

    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}

class EntryMappedField extends ManuallyConfiguredMappedField {
    EntryMappedField(Field field, Class<?> clazz, MappedField mappedField) {
        super(field, clazz);
        super.discover();
        if(AwkwardMapMapper.KEY.equals(field.getName())) {
            realType = mappedField.getMapKeyClass();
            subType = realType.getComponentType();
        } else if(AwkwardMapMapper.VALUE.equals(field.getName())) {
            realType = mappedField.getSubClass();
            subType = mappedField.getSubType() instanceof ParameterizedType ? ((ParameterizedType) mappedField.getSubType()).getActualTypeArguments()[0] : null;
        } else {
            throw new RuntimeException("Entry field is neither key nor entry");
        }

        if (realType.isArray()
                || Collection.class.isAssignableFrom(realType)
                || Map.class.isAssignableFrom(realType)) {

            isSingleValue = false;

            isMap = Map.class.isAssignableFrom(realType);
            isSet = Set.class.isAssignableFrom(realType);
            //for debugging
            isCollection = Collection.class.isAssignableFrom(realType);
            isArray = realType.isArray();

            if (isMap) {
                mapKeyType = ReflectionUtils.getParameterizedType(field, 0);
            }
        }
        try {
            constructor = realType.getDeclaredConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            constructor = null;
        }
    }


}