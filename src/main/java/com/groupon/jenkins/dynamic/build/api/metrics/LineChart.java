package com.groupon.jenkins.dynamic.build.api.metrics;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;
public class LineChart extends Chart {
    private List<Value> values;

    public LineChart(List<Value> values){
        this.values = values;
    }
    @Override
    public ChartType getType() {
        return ChartType.LINE;
    }

    @Override
    public Object value() {
        return values;
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
