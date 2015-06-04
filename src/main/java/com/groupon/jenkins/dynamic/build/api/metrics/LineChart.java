package com.groupon.jenkins.dynamic.build.api.metrics;

import com.google.common.collect.ImmutableMap;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;
public class LineChart extends Chart {
    private List<Value> values;
    private String xLabel;
    private String yLabel;

    public LineChart(List<Value> values, String xLabel, String yLabel){
        this.values = values;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
    }
    @Override
    public ChartType getType() {
        return ChartType.LINE;
    }

    @Override
    public Object getData() {
        return values;
    }

    @Override
    public Object getMetadata() {
        return ImmutableMap.of("x",xLabel, "y",yLabel);
    }

    @ExportedBean(defaultVisibility = 100)
    public static class Value {

        public Value(int x, long y) {
            this.x = x;
            this.y = y;
        }

        private int x;
        private long y;

        @Exported(inline = true)
        public int getX() {
            return this.x;
        }
        @Exported(inline = true)
        public long getY() {
            return this.y;
        }
    }
}
