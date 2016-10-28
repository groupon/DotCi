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

package com.groupon.jenkins.dynamic.build.repository;

import java.util.HashMap;
import java.util.Map;

public class MongoQueryProjectionBuilder {
    private final String[] includes;
    private final HashMap fields;
    private boolean noId;

    public MongoQueryProjectionBuilder(String[] fields) {
        this.includes = fields;
        this.fields = new HashMap();
    }

    public static MongoQueryProjectionBuilder projection(String... fields) {
        return new MongoQueryProjectionBuilder(fields);
    }

    public MongoQueryProjectionBuilder noId() {
        this.noId = true;
        return this;
    }

    public MongoQueryProjectionBuilder field(String key, String value) {
        fields.put(key, value);
        return this;
    }

    public Map get() {
        for (String field : includes) {
            fields.put(field, 1);
        }
        if (noId) {
            fields.put("_id", 0);
        }
        return fields;
    }
}
