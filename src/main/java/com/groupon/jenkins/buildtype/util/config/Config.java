/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.groupon.jenkins.buildtype.util.config;

import com.groupon.jenkins.buildtype.install_packages.buildconfiguration.configvalue.ConfigValue;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private Map config;
    private Map<String,Class<ConfigValue>> configSpec;

    public Config(Map config, Object ... configSpec){
        this.config = config;
        this.configSpec = toSpec(configSpec);
    }

    private Map<String, Class<ConfigValue>> toSpec(Object[] specs) {
        Map<String,Class<ConfigValue>> configSpec = new HashMap<String, Class<ConfigValue>>();
        for(int i = 0 ; i < specs.length; i = i +2){
            configSpec.put((String)specs[i], (Class<ConfigValue>)specs[i+1]);
        }

        return configSpec;
    }

    public <T> T get(String key, Class<T> returnType) {
        ConfigValue<?> configValue = getConfigValue(key);
        return configValue.getValue(returnType);
    }

    public Object get(String key) {
        return config.get(key);
    }
    private ConfigValue<?> getConfigValue(String key){
        Class<ConfigValue> configType = configSpec.get(key);
        Object configValue = config.get(key);
        try {
          return  configType.getConstructor(Object.class).newInstance(configValue);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean containsKey(String key) {
        return config.containsKey(key) && config.get(key) != null;
    }
}
