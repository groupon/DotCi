package com.groupon.jenkins.dynamic.build;

import hudson.Util;
import hudson.matrix.Axis;
import hudson.matrix.Combination;
import hudson.model.Build;
import hudson.model.Queue;

import java.util.List;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.Stapler;

public final class DynamicRunPtr {
    private final Combination combination;
    private transient final DynamicBuild dynamicBuild;

    DynamicRunPtr(Combination c, DynamicBuild dynamicBuild) {
        this.combination = c;
        this.dynamicBuild = dynamicBuild;
    }

    public String toName(List<Axis> axisList) {
        return combination.toString(axisList);
    }

    public Build getRun() {
        return dynamicBuild.getRun(combination);
    }

    public String getNearestRunUrl() {
        @SuppressWarnings("rawtypes")
        Build r = getRun();
        if (r == null) {
            return null;
        }
        if (dynamicBuild.getNumber() == r.getNumber()) {
            return getShortUrl() + "/console";
        }
        return Stapler.getCurrentRequest().getContextPath() + '/' + r.getUrl();
    }

    public String getShortUrl() {
        return Util.rawEncode(combination.toString());
    }

    public String getTooltip() {
        Build r = getRun();
        if (r != null) {
            return r.getIconColor().getDescription();
        }
        Queue.Item item = Jenkins.getInstance().getQueue().getItem(dynamicBuild.getParent().getItem(combination));
        if (item != null) {
            return item.getWhy();
        }
        return null; // fall back
    }
}
