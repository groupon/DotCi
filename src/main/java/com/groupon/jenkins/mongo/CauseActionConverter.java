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
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import hudson.model.CauseAction;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import java.util.ArrayList;
import java.util.List;

public class CauseActionConverter extends TypeConverter implements SimpleValueConverter {

    public CauseActionConverter() {
        super(CauseAction.class);
    }

    @Override
    public CauseAction decode(Class targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        if (fromDBObject == null) return null;

        List causes = new ArrayList();
        List rawList = (List) ((DBObject) fromDBObject).get("causes");
        for (Object obj : rawList) {
            DBObject dbObj = (DBObject) obj;
            Object cause = getMapper().fromDBObject(optionalExtraInfo.getSubClass(), dbObj, getMapper().createEntityCache());
            causes.add(cause);
        }
        return new CauseAction(causes);
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        if (value == null) return null;
        CauseAction action = (CauseAction) value;
        List causes = new BasicDBList();

        for (Object obj : action.getCauses()) {
            causes.add(getMapper().toDBObject(obj));
        }
        return BasicDBObjectBuilder.start("causes", causes).add("className", CauseAction.class.getName()).get();
    }
}
