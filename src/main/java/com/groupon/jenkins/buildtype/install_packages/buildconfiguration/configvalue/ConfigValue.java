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
package com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue;

import com.google.inject.TypeLiteral;

//@formatter:off 
/**
 * Abstraction over value of section in .ci.yml . this could be a list or a map
 * or a string 
 * eg: a) run:echo hello
 *     b) run :
 *         - echo hello 
 *         - echo world 
 *      c) run:
 *          unit: spec unit 
 *          integration: spec unit
 */
//@formatter:on 

public abstract class ConfigValue<T> extends TypeLiteral<T> {
    private Object value;

    public ConfigValue(Object value) {
        this.value = value;
    }

    public void replace(ConfigValue<?> otherConfig) {
        this.value = otherConfig.getValue();
    }

    public boolean isEmpty() {
        return value == null;
    }

    public boolean isValid() {
        return isEmpty() ? true : getRawType().isAssignableFrom(value.getClass());
    }

    public abstract void append(ConfigValue<?> config);

    @SuppressWarnings("unchecked")
    public T getValue() {
        return (T) value;
    }

    protected void setValue(T newValue) {
        this.value = newValue;
    }

    public abstract <T> T getValue(Class<T> returnType);
}
