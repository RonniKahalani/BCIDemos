package org.example.bci;

import org.apache.poi.xddf.usermodel.chart.MarkerStyle;

import java.util.List;

public class ChartDescriptor {

    public String sheetTitle;
    public List<String> columnPatterns;
    public boolean chartType3D;
    public String chartTitle;
    public String xAxisTitle;
    public String yAxisTitle;
    public MarkerStyle markerStyle;

    /**
     * Constructor.
     *
     * @param sheetTitle
     * @param columnPatterns
     * @param chartType3D
     * @param chartTitle
     * @param xAxisTitle
     * @param yAxisTitle
     * @param markerStyle
     */
    public ChartDescriptor(String sheetTitle, List<String> columnPatterns, boolean chartType3D, String chartTitle, String xAxisTitle, String yAxisTitle, MarkerStyle markerStyle) {
        this.sheetTitle = sheetTitle;
        this.columnPatterns = columnPatterns;
        this.chartType3D = chartType3D;
        this.chartTitle = chartTitle;
        this.xAxisTitle = xAxisTitle;
        this.yAxisTitle = yAxisTitle;
        this.markerStyle = markerStyle;
    }
}
