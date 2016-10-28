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
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.EntityCache;
import org.mongodb.morphia.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * For Maps which have a key type that can not be mapped to a MongoDB field name,
 * we can save a list of key-value pairs.
 */
class MapKeyValueMapper implements CustomMapper {
    public static final String KEY = "key";
    public static final String VALUE = "value";

    @Override
    public void toDBObject(final Object entity, final MappedField mf, final DBObject dbObject, final Map<Object, DBObject> involvedObjects, final Mapper mapper) {
        final String name = mf.getNameToStore();

        final Map<?, ?> awkwardMap = (Map<?, ?>) mf.getFieldValue(entity);
        final List out = new ArrayList();

        for (final Map.Entry entry : awkwardMap.entrySet()) {
            final DBObject mappedEntry = new BasicDBObject();

            try {
                final Field key = entry.getClass().getDeclaredField(KEY);
                final EntryMappedField keyField = new EntryMappedField(key, entry.getClass(), mf);

                final Field value = entry.getClass().getDeclaredField(VALUE);
                final EntryMappedField valueField = new EntryMappedField(value, entry.getClass(), mf);

                mapper.getOptions().getDefaultMapper().toDBObject(entry, keyField, mappedEntry, involvedObjects, mapper);
                mapper.getOptions().getDefaultMapper().toDBObject(entry, valueField, mappedEntry, involvedObjects, mapper);

                out.add(mappedEntry);
            } catch (final NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        dbObject.put(name, out);
    }

    @Override
    public void fromDBObject(final DBObject dbObject, final MappedField mf, final Object entity, final EntityCache cache, final Mapper mapper) {
        final BasicDBList entriesList = (BasicDBList) dbObject.get(mf.getNameToStore());

        final Map map = mapper.getOptions().getObjectFactory().createMap(mf);

        for (final Object obj : entriesList) {
            final DBObject listEntryDbObj = (DBObject) obj;
            //Map.Entry entry = (Map.Entry) mapper.getOptions().getObjectFactory().createInstance(Map.Entry.class);
            final Pair entry = new Pair();
            try {
                final Field key = entry.getClass().getDeclaredField(KEY);
                final EntryMappedField keyField = new EntryMappedField(key, entry.getClass(), mf);

                final Field value = entry.getClass().getDeclaredField(VALUE);
                final EntryMappedField valueField = new EntryMappedField(value, entry.getClass(), mf);

                mapper.getOptions().getEmbeddedMapper().fromDBObject(listEntryDbObj, keyField, entry, cache, mapper);
                mapper.getOptions().getEmbeddedMapper().fromDBObject(listEntryDbObj, valueField, entry, cache, mapper);

                map.put(entry.getKey(), entry.getValue());

            } catch (final NoSuchFieldException e) {
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
        return this.key;
    }

    public Object getValue() {
        return this.value;
    }
}

class EntryMappedField extends ManuallyConfiguredMappedField {
    EntryMappedField(final Field field, final Class<?> clazz, final MappedField mappedField) {
        super(field, clazz);
        super.discover();
        if (MapKeyValueMapper.KEY.equals(field.getName())) {
            this.realType = mappedField.getMapKeyClass();
            this.subType = this.realType.getComponentType();
        } else if (MapKeyValueMapper.VALUE.equals(field.getName())) {
            this.realType = mappedField.getSubClass();
            this.subType = mappedField.getSubType() instanceof ParameterizedType ? ((ParameterizedType) mappedField.getSubType()).getActualTypeArguments()[0] : null;
        } else {
            throw new RuntimeException("Entry field is neither key nor entry");
        }

        if (this.realType.isArray()
            || Collection.class.isAssignableFrom(this.realType)
            || Map.class.isAssignableFrom(this.realType)) {

            this.isSingleValue = false;

            this.isMap = Map.class.isAssignableFrom(this.realType);
            this.isSet = Set.class.isAssignableFrom(this.realType);
            //for debugging
            this.isCollection = Collection.class.isAssignableFrom(this.realType);
            this.isArray = this.realType.isArray();

            if (this.isMap) {
                this.mapKeyType = ReflectionUtils.getParameterizedType(field, 0);
            }
        }
        try {
            this.constructor = this.realType.getDeclaredConstructor();
            this.constructor.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            this.constructor = null;
        }
    }


}
