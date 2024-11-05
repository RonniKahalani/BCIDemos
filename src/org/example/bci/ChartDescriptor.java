package org.example.bci;

public class ChartDescriptor {


    public String sheetTitle;
    public String columnPattern;
    public boolean chartType3D;
    public String chartTitle;
    public String xAxisTitle;
    public  String yAxisTitle;

    public ChartDescriptor(String sheetTitle, String columnPattern, boolean chartType3D, String chartTitle, String xAxisTitle, String yAxisTitle) {
        this.sheetTitle = sheetTitle;
        this.columnPattern = columnPattern;
        this.chartType3D = chartType3D;
        this.chartTitle = chartTitle;
        this.xAxisTitle = xAxisTitle;
        this.yAxisTitle = yAxisTitle;
    }
}
