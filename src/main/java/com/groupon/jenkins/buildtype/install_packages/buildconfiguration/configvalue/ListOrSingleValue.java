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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListOrSingleValue<T> extends ConfigValue<Object> {
    public ListOrSingleValue(Object ... one){
       super(null);
    }

    public ListOrSingleValue(Object value) {
        super(toList(value));
    }

    private static List<?> toList(Object value) {
        if (value instanceof List) {
            return (List<?>) value;
        } else if (value == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(value);
        }

    }

    @SuppressWarnings("unchecked")
    public List<T> getValues() {
        return (List<T>) getValue();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() || size() == 0;
    }

    @Override
    public void append(ConfigValue<?> otherConfig) {
        ArrayList<T> newValue = new ArrayList<T>();
        if (!isEmpty()) {
            newValue.addAll(getValues());
        }
        if (!otherConfig.isEmpty()) {
            newValue.addAll(((ListOrSingleValue) otherConfig).getValues());
        }
        setValue(newValue);

    }

    @Override
    public <R> R getValue(Class<R> returnType) {
        if(List.class.isAssignableFrom(returnType)){
            return (R) getValues();
        }
        if(String.class.isAssignableFrom(returnType)){
            return (R)getValues().get(0);
        }
        return (R) getValue();
    }

    public int size() {
        return getValues().size();
    }

}
