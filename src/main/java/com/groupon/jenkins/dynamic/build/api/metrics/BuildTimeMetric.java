package com.groupon.jenkins.dynamic.build.api.metrics;

import com.groupon.jenkins.SetupConfig;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import hudson.Extension;

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
        List<DbBackedBuild> builds = SetupConfig.get().getDynamicBuildRepository().getSuccessfulBuilds(getProject(), "master", cal, Calendar.getInstance());;

        for(DbBackedBuild build : builds){
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
}
