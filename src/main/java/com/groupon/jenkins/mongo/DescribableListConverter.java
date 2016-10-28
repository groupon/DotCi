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
import hudson.model.Saveable;
import hudson.util.DescribableList;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import java.util.ArrayList;
import java.util.List;

public class DescribableListConverter extends TypeConverter implements SimpleValueConverter {

    public DescribableListConverter() {
        super(DescribableList.class);
    }

    @Override
    public Object decode(Class targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        if (fromDBObject == null) return null;

        BasicDBList rawList = (BasicDBList) fromDBObject;

        List list = new ArrayList();
        for (Object obj : rawList) {
            DBObject dbObj = (DBObject) obj;
            list.add(getMapper().fromDBObject(optionalExtraInfo.getSubClass(), dbObj, getMapper().createEntityCache()));
        }

        Saveable owner = null; // TODO figure out how to associate the deserialized project here

        return new DescribableList(owner, list);
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        if (value == null) return null;

        DescribableList describableList = (DescribableList) value;

        BasicDBList convertedList = new BasicDBList();

        for (Object obj : describableList.toList()) {
            convertedList.add(getMapper().toDBObject(obj));
        }

        return convertedList;
    }
}
