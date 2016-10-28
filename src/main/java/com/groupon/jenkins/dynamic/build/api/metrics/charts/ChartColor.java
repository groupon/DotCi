package com.groupon.jenkins.dynamic.build.api.metrics.charts;

public enum ChartColor {
    BLUE(
        "rgba(151,187,205,0.2)",
        "rgba(151,187,205,1)",
        "rgba(151,187,205,1)",
        "#fff",
        "#fff",
        "rgba(151,187,205,0.8)"
    ),
    LIGHT_GREY(
        "rgba(220,220,220,0.2)",
        "rgba(220,220,220,1)",
        "rgba(220,220,220,1)",
        "#fff",
        "#fff",
        "rgba(220,220,220,0.8)"
    ),
    RED(
        "rgba(247,70,74,0.2)",
        "rgba(247,70,74,1)",
        "rgba(247,70,74,1)",
        "#fff",
        "#fff",
        "rgba(247,70,74,0.8)"
    ),
    GREEN("rgba(70,191,189,0.2)",
        "rgba(70,191,189,1)",
        "rgba(70,191,189,1)",
        "#fff",
        "#fff",
        "rgba(70,191,189,0.8)"),
    YELLOW(
        "rgba(253,180,92,0.2)",
        "rgba(253,180,92,1)",
        "rgba(253,180,92,1)",
        "#fff",
        "#fff",
        "rgba(253,180,92,0.8)"
    ),
    GREY(
        "rgba(148,159,177,0.2)",
        "rgba(148,159,177,1)",
        "rgba(148,159,177,1)",
        "#fff",
        "#fff",
        "rgba(148,159,177,0.8)"
    ),
    DARK_GREY(
        "rgba(77,83,96,0.2)",
        "rgba(77,83,96,1)",
        "rgba(77,83,96,1)",
        "#fff",
        "#fff",
        "rgba(77,83,96,1)"
    );


    public String fillColor;
    public String strokeColor;
    public String pointColor;
    public String pointStrokeColor;
    public String pointHighlightFill;
    public String pointHighlightStroke;

    ChartColor(String fillColor, String strokeColor, String pointColor, String pointStrokeColor, String pointHighlightFill, String pointHighlightStroke) {

        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.pointColor = pointColor;
        this.pointStrokeColor = pointStrokeColor;
        this.pointHighlightFill = pointHighlightFill;
        this.pointHighlightStroke = pointHighlightStroke;
    }
}
