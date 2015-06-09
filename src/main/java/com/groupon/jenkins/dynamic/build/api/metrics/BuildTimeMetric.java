package com.groupon.jenkins.dynamic.build.api.metrics;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.api.metrics.charts.LineChart;
import hudson.Extension;
import hudson.model.Result;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Extension
public class BuildTimeMetric extends JobMetric {

    private List<LineChart.Value> getValues(){
        List<LineChart.Value> buildTimes = new ArrayList<LineChart.Value>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Iterable<DynamicBuild> builds = SetupConfig.get().getDynamicBuildRepository().getBuilds(getProject(), getBranch(), getBuildCount(), Result.SUCCESS);
        for(DynamicBuild build : builds){
            LineChart.Value buildTime = new LineChart.Value(build.getNumber(), TimeUnit.MILLISECONDS.toMinutes(build.getDuration()));
            buildTimes.add(buildTime);
        }
        return buildTimes;
    }

    @Override
    public Chart getChart() {
        return new LineChart(getValues(),"Build Number","Build Time(mins)");
    }

    @Override
    public String getName() {
        return "Build Times";
    }

    @Override
    public boolean isApplicable() {
        return true;
    }
}
