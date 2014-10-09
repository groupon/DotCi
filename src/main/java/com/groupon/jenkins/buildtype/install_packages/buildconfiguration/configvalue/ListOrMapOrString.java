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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListOrMapOrString extends ConfigValue<Object> {

    public ListOrMapOrString(Object configValue) {
        super(convertMapWithSingleValueIntoValue(configValue));
    }

    private static Object convertMapWithSingleValueIntoValue(Object configValue) {
        if (configValue instanceof Map) {
            Map<String, String> valueAsMap = (Map<String, String>) configValue;
            if (valueAsMap.size() == 1) {
                return valueAsMap.values().iterator().next();
            }
        }
        return configValue;
    }

    @Override
    public void append(ConfigValue<?> config) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getValue(Class<T> returnType) {
        if(Map.class.isAssignableFrom(returnType)){
          return (T) getMap();
        }
        if(List.class.isAssignableFrom(returnType)){
            return (T) getValuesList();
        }
        if(String.class.isAssignableFrom(returnType)){
            return (T) getValue();
        }
     throw new IllegalArgumentException("Return Type :" + returnType + " not supported.");
    }

    public boolean isMap() {
        return getValue() instanceof Map;
    }

    @SuppressWarnings("unchecked")
    public Map<String, ?> getMap() {
        return (Map<String, ?>) getValue();
    }

    public List<String> getValues(String key) {
        return toList(getMap().get(key));
    }

    @SuppressWarnings("unchecked")
    private List<String> toList(Object keyValue) {
        if (keyValue instanceof List) {
            return (List<String>) keyValue;
        } else if (keyValue == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(keyValue.toString());
        }
    }

    public List<String> getValuesList() {
        return toList(getValue());
    }

    public Set<String> getKeys() {
        return getMap().keySet();
    }

}
