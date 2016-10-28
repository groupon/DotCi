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
import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

public class AxisListConverter extends TypeConverter implements SimpleValueConverter {

    public AxisListConverter() {
        super(AxisList.class);
    }

    @Override
    public Object decode(Class targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        if (fromDBObject == null) return null;

        BasicDBList rawList = (BasicDBList) fromDBObject;

        AxisList axisList = new AxisList();
        for (Object obj : rawList) {
            DBObject dbObj = (DBObject) obj;
            axisList.add((Axis) getMapper().fromDBObject(optionalExtraInfo.getSubClass(), dbObj, getMapper().createEntityCache()));
        }

        return axisList;
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        if (value == null) return null;

        AxisList axisList = (AxisList) value;

        BasicDBList convertedList = new BasicDBList();

        for (Axis axis : axisList) {
            convertedList.add(getMapper().toDBObject(axis));
        }

        return convertedList;
    }
}
