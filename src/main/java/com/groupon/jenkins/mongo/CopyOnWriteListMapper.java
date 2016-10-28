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
import com.mongodb.DBObject;
import hudson.util.CopyOnWriteList;
import org.mongodb.morphia.mapping.CustomMapper;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.EntityCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class CopyOnWriteListMapper implements CustomMapper {
    @Override
    public void toDBObject(Object entity, MappedField mf, DBObject dbObject, Map<Object, DBObject> involvedObjects, Mapper mapper) {
        final String name = mf.getNameToStore();
        CopyOnWriteList copyOnWriteList = (CopyOnWriteList) mf.getFieldValue(entity);
        List core = new ArrayList();

        mf.getSubType();

        for (Object obj : copyOnWriteList) {
            core.add(mapper.toDBObject(obj, involvedObjects));
        }

        dbObject.put(name, core);
    }

    @Override
    public void fromDBObject(DBObject dbObject, MappedField mf, Object entity, EntityCache cache, Mapper mapper) {
        BasicDBList cowlist = (BasicDBList) dbObject.get(mf.getNameToStore());

        if (cowlist == null)
            throw new IllegalArgumentException("Improperly formatted DBObject for CopyOnWriteList");

        List core = new ArrayList();
        for (Object obj : cowlist) {
            DBObject listEntryDbObj = (DBObject) obj;

            // Hack until we can coax MappedField to understand what CopyOnWriteList is. Eliminate as soon as possible.
            // Currently mf.getSubType() is null because MappedField does not use Iterable to determine a list and thus
            // does not check for subtypes.
            Class clazz = mapper.getOptions().getObjectFactory().createInstance(mapper, mf, listEntryDbObj).getClass();

            core.add(mapper.fromDBObject(clazz, listEntryDbObj, cache));
        }
        mf.setFieldValue(entity, new CopyOnWriteList(core));
    }
}
