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

import org.mongodb.morphia.mapping.MappedField;

import java.lang.reflect.Field;

/**
 * Hack to address issues in Jenkins mapping
 * These methods are copied directly from Morphia 1.07 so we can get around the method access limits.
 * <p>
 * Deprecated because we want to use the original classes.
 */
@Deprecated
class ManuallyConfiguredMappedField extends MappedField {
    ManuallyConfiguredMappedField(final Field f, final Class<?> clazz) {
        f.setAccessible(true);
        this.field = f;
        this.persistedClass = clazz;
    }

    public void discover() {
        super.discover();
    }
}

