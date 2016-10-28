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

package com.groupon.jenkins.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class ResourceUtils {
    public static String readResource(Class<?> resourceClass, String resourceName) {
        return readFile(resourceClass, getTemplateFile(resourceClass, resourceName));
    }

    private static String getTemplateFile(Class<?> resourceClass, String resourceName) {
        while (resourceClass != Object.class && resourceClass != null) {
            String name = resourceClass.getName().replace('.', '/').replace('$', '/') + "/" + resourceName;
            if (resourceClass.getClassLoader().getResource(name) != null)
                return '/' + name;
            resourceClass = resourceClass.getSuperclass();
        }
        return null;
    }

    private static String readFile(Class<?> resourceClass, String ymlResource) {
        InputStream base = null;
        try {
            base = resourceClass.getResourceAsStream(ymlResource);
            return IOUtils.toString(base);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(base);
        }
    }
}
