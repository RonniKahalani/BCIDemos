package org.example.bci.visualizer;

import org.apache.poi.xddf.usermodel.chart.MarkerStyle;

import java.util.List;

/**
 * Describes a chart to be created.
 */
public class ChartDescriptor {

    public final String sheetTitle;
    public final List<String> columnPatterns;
    public final boolean chartType3D;
    public final String chartTitle;
    public final String xAxisTitle;
    public final String yAxisTitle;
    public final MarkerStyle markerStyle;

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
