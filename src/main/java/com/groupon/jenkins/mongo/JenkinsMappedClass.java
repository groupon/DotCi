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

import hudson.util.CopyOnWriteList;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.mapping.MappedClass;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;


class JenkinsMappedClass extends MappedClass {

    /**
     * constructor
     *
     * @param clazz
     * @param mapper
     */
    public JenkinsMappedClass(Class<?> clazz, Mapper mapper) {
        super(clazz, mapper);
    }

    @Override
    public void validate() {
        basicValidate();
    }

    @Override
    protected void discover() {
        for (final Field field : ReflectionUtils.getDeclaredAndInheritedFields(getClazz(), true)) {
            field.setAccessible(true);
            final int fieldMods = field.getModifiers();
            if (field.isAnnotationPresent(Transient.class)
                    || field.isSynthetic() && (fieldMods & Modifier.TRANSIENT) == Modifier.TRANSIENT
                    || getMapper().getOptions().isActLikeSerializer() && ((fieldMods & Modifier.TRANSIENT) == Modifier.TRANSIENT)
                    || getMapper().getOptions().isIgnoreFinals() && ((fieldMods & Modifier.FINAL) == Modifier.FINAL)) {
                // ignore these
            } else {
                getPersistenceFields().add(mapField(field));
            }

        }
    }

    protected MappedField mapField(Field field) {
        ManuallyConfiguredMappedField mf = new ManuallyConfiguredMappedField(field, getClazz());
        mf.discover();

        return mf;
    }
}

class CopyOnWriteListMappedClass extends JenkinsMappedClass {

    /**
     * constructor
     *
     * @param mapper
     */
    public CopyOnWriteListMappedClass(Mapper mapper) {
        super(CopyOnWriteList.class, mapper);
    }

    @Override
    protected MappedField mapField(Field field) {
        if("core".equals(field.getName())) {
            ManuallyConfiguredMappedField mf = new ManuallyConfiguredMappedField(field, getClazz()) {
                @Override
                public void discover() {
                    realType = field.getType();
                    isMap = false;
                    isSet = false;
                    isSingleValue = false;
                    isArray = realType.isArray();
                    isCollection = Collection.class.isAssignableFrom(realType);
                    constructor = null;
                    isMongoType = false;
                    subType = realType.getComponentType();
                }
            };

            mf.discover();
            return mf;
        }
        return super.mapField(field);
    }
}