package org.example.bci.visualizer;

import brainflow.BoardIds;
import brainflow.BrainFlowInputParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Visualizes a BCI data extract into an Excel file.
 */
public class BCIVisualizer {
    private static final Logger logger = LogManager.getLogger(BCIVisualizer.class);

    final static String SAMPLE_TITLE = "Sample";
    final static String VALUE_TITLE = "Value";

    private static DataExtractor dataExtractor;
    private static List<ChartDescriptor> chartDescriptors;

    /**
     * Main entry point.
     *
     * @param args to the main program entry point.
     * @throws Exception from the BrainFlow API or Excel export.
     */
    public static void main(String[] args) throws Exception {

        logger.info("Starting BCI Visualizer");

        // Parse the command line parameters.
        BrainFlowInputParams params = new BrainFlowInputParams();
        int boardId = ParamParser.parseParams(args, params);

        // Get the data from the device.
        extractData(boardId, params);
        // Configure the charts.
        configureCharts();
        // Export the Excel file.
        exportExcelFile();
    }

    /**
     * Extracts the data from the device.
     *
     * @throws Exception from the BrainFlow API.
     */
    private static void extractData(int boardId, BrainFlowInputParams params) throws Exception {
        dataExtractor = new DataExtractor(boardId, params, DataExtractor.BUFFER_SIZE, DataExtractor.WAIT_MILLIS, DataExtractor.SAMPLE_COUNT);
        dataExtractor.extractData();
    }

    /**
     * Configures the Excel charts to be created.
     */
    private static void configureCharts() {

        String[] titles = {"Frontal", "Central", "Occipital", "Gyro"};
        chartDescriptors = List.of(
                new ChartDescriptor(titles[0], List.of("(?i)^F.*$"), false, titles[0], SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT),
                new ChartDescriptor(titles[1], List.of("(?i)^C.*$"), false, titles[1], SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT),
                new ChartDescriptor(titles[2], List.of("(?i)^O.*$", "(?i)^PO.*$", "(?i)^Pz.*$"), false, titles[2], SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT),
                new ChartDescriptor(titles[3], List.of("(?i)^Gyro.*$"), false, titles[3], SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
    }

    /**
     * Exports the Excel file.
     *
     * @throws Exception from the Excel export.
     */
    private static void exportExcelFile() throws Exception {
        String fileName = "BrainFlow-" + BoardIds.from_code(dataExtractor.getBoardId()) + "-" + new SimpleDateFormat("yyyyMMddHHmm'.xlsx'").format(new Date());
        ExcelExporter exporter = new ExcelExporter();
        exporter.generateExcelFile(fileName, dataExtractor, chartDescriptors);
    }

}
