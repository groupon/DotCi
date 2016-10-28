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
    public JenkinsMappedClass(final Class<?> clazz, final Mapper mapper) {
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

    protected MappedField mapField(final Field field) {
        final ManuallyConfiguredMappedField mf = new ManuallyConfiguredMappedField(field, getClazz());
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
    public CopyOnWriteListMappedClass(final Mapper mapper) {
        super(CopyOnWriteList.class, mapper);
    }

    @Override
    protected MappedField mapField(final Field field) {
        if ("core".equals(field.getName())) {
            final ManuallyConfiguredMappedField mf = new ManuallyConfiguredMappedField(field, getClazz()) {
                @Override
                public void discover() {
                    this.realType = this.field.getType();
                    this.isMap = false;
                    this.isSet = false;
                    this.isSingleValue = false;
                    this.isArray = this.realType.isArray();
                    this.isCollection = Collection.class.isAssignableFrom(this.realType);
                    this.constructor = null;
                    this.isMongoType = false;
                    this.subType = this.realType.getComponentType();
                }
            };

            mf.discover();

            return mf;
        }
        return super.mapField(field);
    }
}
