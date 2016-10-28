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

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import java.util.ArrayList;
import java.util.List;

public class ParametersDefinitionPropertyCoverter extends TypeConverter implements SimpleValueConverter {
    public ParametersDefinitionPropertyCoverter() {
        super(ParametersDefinitionProperty.class);
    }

    @Override
    public Object decode(Class targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        if (fromDBObject == null) return null;

        List<DBObject> list = (List) fromDBObject;

        List<ParameterDefinition> parameterDefinitions = new ArrayList<ParameterDefinition>();

        // TODO parsing checks
        for (DBObject dbObject : list) {
            ParameterDefinition definition = (ParameterDefinition) getMapper()
                .fromDBObject(ParameterDefinition.class, dbObject, getMapper().createEntityCache());

            parameterDefinitions.add(definition);
        }

        return new ParametersDefinitionProperty(parameterDefinitions);
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        if (value == null) return null;

        ParametersDefinitionProperty parametersDefinitionProperty = (ParametersDefinitionProperty) value;

        BasicDBList parameters = new BasicDBList();
        for (ParameterDefinition definition : parametersDefinitionProperty.getParameterDefinitions()) {
            parameters.add(getMapper().toDBObject(definition));
        }

        return parameters;
    }
}
