package org.example.bci.visualizer;

import org.apache.poi.xddf.usermodel.chart.MarkerStyle;

import java.util.List;

/**
 * Describes a chart to be created.
 */
public record ChartDescriptor(String sheetTitle, List<String> columnPatterns, boolean chartType3D, String chartTitle,
                              String xAxisTitle, String yAxisTitle, MarkerStyle markerStyle) {

    /**
     * Constructor.
     *
     * @param sheetTitle     the title of the sheet.
     * @param columnPatterns the column patterns to include in the chart.
     * @param chartType3D    whether the chart is 3D.
     * @param chartTitle     the title of the chart.
     * @param xAxisTitle     the title of the x axis.
     * @param yAxisTitle     the title of the y axis.
     * @param markerStyle    the marker style.
     */
    public ChartDescriptor {
    }
}
