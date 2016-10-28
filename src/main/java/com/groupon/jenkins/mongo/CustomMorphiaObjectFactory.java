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

import com.mongodb.DBObject;
import org.mongodb.morphia.mapping.DefaultCreator;
import org.mongodb.morphia.mapping.MappingException;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/*
 * Credit Johan Haleby
 * http://www.jayway.com/2012/02/28/configure-morphia-to-work-without-a-default-constructor/
 */

public class CustomMorphiaObjectFactory extends DefaultCreator {
    private final ClassLoader classloader;
    private final Objenesis objenesis;

    public CustomMorphiaObjectFactory(final ClassLoader classloader) {
        this.objenesis = new ObjenesisStd();
        this.classloader = classloader;
    }

    @Override
    public Object createInstance(final Class clazz) {
        try {
            final Constructor constructor = getNoArgsConstructor(clazz);
            if (constructor != null) {
                return constructor.newInstance();
            }
            try {
                return clazz == List.class ? new ArrayList() : this.objenesis.newInstance(clazz);
            } catch (final Exception e) {
                throw new MappingException("Failed to instantiate " + clazz.getName(), e);
            }
        } catch (final Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    @Override
    protected ClassLoader getClassLoaderForClass(final String clazz, final DBObject object) {
        return this.classloader;
    }

    private Constructor getNoArgsConstructor(final Class ctorType) {
        try {
            final Constructor ctor = ctorType.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor;
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }
}
