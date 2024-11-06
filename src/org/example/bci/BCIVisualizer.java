package org.example.bci;

import org.apache.poi.xddf.usermodel.chart.MarkerStyle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Visualizes a BCI data extract into an Excel file.
 */
public class BCIVisualizer {

    final static String SAMPLE_TITLE = "Sample";
    final static String VALUE_TITLE = "Value";

    private static DataExtractor dataExtractor;
    private static List<ChartDescriptor> chartDescriptors;

    /**
     * Main entry point.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Get the data from the device.
        extractData();
        // Configure the charts.
        configureCharts();
        // Export the Excel file.
        exportExcelFile();
    }

    /**
     * Extracts the data from the device.
     *
     * @throws Exception
     */
    private static void extractData() throws Exception {
        dataExtractor = new DataExtractor();
        dataExtractor.extractData();
    }

    /**
     * Configures the Excel charts to be created.
     */
    private static void configureCharts() {
        chartDescriptors = new ArrayList<>();

        String prefix = "Frontal";
        chartDescriptors.add( new ChartDescriptor(prefix, "(?i)^" + prefix + ".*$", false, prefix, SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
        prefix = "Central";
        chartDescriptors.add( new ChartDescriptor(prefix, "(?i)^" + prefix + ".*$", false, prefix, SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
        prefix = "Gyro";
        chartDescriptors.add( new ChartDescriptor(prefix, "(?i)^" + prefix + ".*$", false, prefix, SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
    }

    /**
     * Exports the Excel file.
     *
     * @throws Exception
     */
    private static void exportExcelFile() throws Exception {
        String fileName = "BrainFlow-" + dataExtractor.getBoardId() + "-" + new SimpleDateFormat("yyyyMMddHHmm'.xlsx'").format(new Date());
        ExcelExporter exporter = new ExcelExporter();
        exporter.generateExcelFile(fileName, dataExtractor, chartDescriptors);
    }

}
