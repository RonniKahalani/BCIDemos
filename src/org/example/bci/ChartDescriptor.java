package org.example.bci;

import org.apache.poi.xddf.usermodel.chart.MarkerStyle;

public class ChartDescriptor {


    public String sheetTitle;
    public String columnPattern;
    public boolean chartType3D;
    public String chartTitle;
    public String xAxisTitle;
    public  String yAxisTitle;
    public MarkerStyle markerStyle;

    public ChartDescriptor(String sheetTitle, String columnPattern, boolean chartType3D, String chartTitle, String xAxisTitle, String yAxisTitle, MarkerStyle markerStyle) {
        this.sheetTitle = sheetTitle;
        this.columnPattern = columnPattern;
        this.chartType3D = chartType3D;
        this.chartTitle = chartTitle;
        this.xAxisTitle = xAxisTitle;
        this.yAxisTitle = yAxisTitle;
        this.markerStyle = markerStyle;
    }
}
