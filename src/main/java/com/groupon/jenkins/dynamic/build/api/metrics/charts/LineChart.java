package com.groupon.jenkins.dynamic.build.api.metrics.charts;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

@ExportedBean(defaultVisibility = 100)
public class LineChart extends Chart {
    private List<String> labels;
    private List<DataSet> dataSets;
    private String xLabel;
    private String yLabel;

    public LineChart(List<String> labels, List<DataSet> dataSets, String xLabel, String yLabel) {
        this.labels = labels;
        this.dataSets = dataSets;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
    }

    @Exported(inline = true)
    public List<DataSet> getDataSets() {
        return dataSets;
    }

    @Exported(inline = true)
    public List<String> getLabels() {
        return labels;
    }

    @Exported(inline = true)
    public String getxLabel() {
        return xLabel;
    }

    @Exported(inline = true)
    public String getyLabel() {
        return yLabel;
    }

    @Override
    public ChartType getType() {
        return ChartType.LINE;
    }


    @ExportedBean
    public static class DataSet {
        private String label;
        private String fillColor;
        private String strokeColor;
        private String pointColor;
        private String pointStrokeColor;
        private String pointHighlightFill;
        private String pointHighlightStroke;
        private List<Long> data;

        public DataSet(String label, String fillColor, String strokeColor, String pointColor, String pointStrokeColor, String pointHighlightFill, String pointHighlightStroke, List<Long> data) {
            this.label = label;
            this.fillColor = fillColor;
            this.strokeColor = strokeColor;
            this.pointColor = pointColor;
            this.pointStrokeColor = pointStrokeColor;
            this.pointHighlightFill = pointHighlightFill;
            this.pointHighlightStroke = pointHighlightStroke;
            this.data = data;
        }

        public DataSet(String label, List<Long> data) {
            this(label, data, ChartColor.DARK_GREY);
        }
        public DataSet(String label, List<Long> data, ChartColor color) {
            this(label, color.fillColor, color.strokeColor, color.pointColor, color.pointStrokeColor, color.pointHighlightFill, color.pointHighlightStroke, data);
        }

        @Exported(inline = true)
        public String getLabel() {
            return label;
        }

        @Exported(inline = true)
        public String getFillColor() {
            return fillColor;
        }

        @Exported(inline = true)
        public String getStrokeColor() {
            return strokeColor;
        }

        @Exported(inline = true)
        public String getPointColor() {
            return pointColor;
        }

        @Exported(inline = true)
        public String getPointStrokeColor() {
            return pointStrokeColor;
        }

        @Exported(visibility = 1)
        public String getPointHighlightFill() {
            return pointHighlightFill;
        }

        @Exported(inline = true)
        public String getPointHighlightStroke() {
            return pointHighlightStroke;
        }

        @Exported(inline = true)
        public List<Long> getData() {
            return data;
        }

    }
}
