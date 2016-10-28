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
package com.groupon.jenkins.util;

import com.google.common.collect.ForwardingMap;
import groovy.text.GStringTemplateEngine;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

public class GroovyYamlTemplateProcessor {

    private final String config;
    private final Map envVars;

    public GroovyYamlTemplateProcessor(String config, Map<String, Object> envVars) {
        this.config = config;
        this.envVars = new HashMap(envVars);
        this.envVars.remove("PATH");
    }

    public Map getConfig() {
        GStringTemplateEngine engine = new GStringTemplateEngine();
        String yaml = null;

        try {
            yaml = engine.createTemplate(config).make(new MissingPropForwardingMap(envVars)).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (Map) new Yaml().load(yaml);
    }

    private static class MissingPropForwardingMap extends ForwardingMap<String, Object> {

        private final Map delegate;

        public MissingPropForwardingMap(Map envVars) {
            this.delegate = envVars;
        }

        @Override
        public boolean containsKey(Object key) {
            return true;
        }

        @Override
        public Object get(Object key) {
            Object value = super.get(key);
            if (value == null) {
                return ((String) key).startsWith("DOTCI") ? null : "$" + key;
            }
            return value;
        }

        @Override
        protected Map delegate() {
            return delegate;
        }

    }
}
