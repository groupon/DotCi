package com.groupon.jenkins.dynamic.build.api.metrics;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.api.metrics.charts.Chart;
import com.groupon.jenkins.dynamic.build.api.metrics.charts.LineChart;
import hudson.Extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Extension
public class BuildTimeMetric extends JobMetric {


    @Override
    public Chart getChart() {
        final Iterable<DynamicBuild> builds = getBuilds(getQuery());
        final List<String> buildNumbers = new ArrayList<>();
        final List<Long> buildTimes = new ArrayList<>();
        for (final DynamicBuild build : builds) {
            buildNumbers.add(build.getNumber() + "");
            buildTimes.add(TimeUnit.MILLISECONDS.toMinutes(build.getDuration()));
        }
        return new LineChart(buildNumbers,
            Arrays.asList(new LineChart.DataSet("Build Time", buildTimes)),
            "Build Number", "Build Time(mins)");
    }

    @Override
    public String getTitle() {
        return "Build Times";
    }

    @Override
    public boolean isApplicable() {
        return true;
    }
}
