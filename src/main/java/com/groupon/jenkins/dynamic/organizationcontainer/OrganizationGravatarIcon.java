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
package com.groupon.jenkins.dynamic.organizationcontainer;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.AbstractStatusIcon;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.Stapler;

public class OrganizationGravatarIcon extends AbstractStatusIcon implements Describable<OrganizationGravatarIcon>, ExtensionPoint {

    private final transient String orgName;

    public OrganizationGravatarIcon(final String orgName) {
        this.orgName = orgName;

    }

    public String getImageOf(final String size) {
        return Stapler.getCurrentRequest().getContextPath() + Hudson.RESOURCE_PATH + "/images/" + size + "/folder.png";
    }

    public String getDescription() {
        return this.orgName;
    }

    public Descriptor<OrganizationGravatarIcon> getDescriptor() {
        return Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    @Extension(ordinal = 100)
    public static class DescriptorImpl extends Descriptor<OrganizationGravatarIcon> {
        public static DescriptorExtensionList<OrganizationGravatarIcon, DescriptorImpl> all() {
            return Hudson.getInstance().<OrganizationGravatarIcon, DescriptorImpl>getDescriptorList(OrganizationGravatarIcon.class);
        }

        @Override
        public String getDisplayName() {
            return "Default Icon";
        }
    }

}
