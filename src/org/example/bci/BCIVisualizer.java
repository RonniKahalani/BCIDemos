package org.example.bci;

import brainflow.AggOperations;
import brainflow.BoardIds;
import brainflow.BrainFlowInputParams;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
        extractData(args);
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
    private static void extractData(String[] args) throws Exception {
        BrainFlowInputParams params = new BrainFlowInputParams();
        int boardId = ParamParser.parseParams(args, params);

        dataExtractor = new DataExtractor(boardId, params, DataExtractor.BUFFER_SIZE, DataExtractor.WAIT_MILLIS, DataExtractor.SAMPLE_COUNT);
        dataExtractor.extractData();
    }

    /**
     * Configures the Excel charts to be created.
     */
    private static void configureCharts() {

        String[] titles = {"Frontal", "Central", "Occipital", "Gyro"};
        chartDescriptors = new ArrayList<>();
        chartDescriptors.add( new ChartDescriptor(titles[0], List.of("(?i)^F.*$"), false, titles[0], SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
        chartDescriptors.add( new ChartDescriptor(titles[1], List.of("(?i)^C.*$"), false, titles[1], SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
        chartDescriptors.add( new ChartDescriptor(titles[2], List.of("(?i)^O.*$", "(?i)^PO.*$", "(?i)^Pz.*$"), false, titles[2], SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
        chartDescriptors.add( new ChartDescriptor(titles[3], List.of("(?i)^Gyro.*$"), false, titles[3], SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
    }

    /**
     * Exports the Excel file.
     *
     * @throws Exception
     */
    private static void exportExcelFile() throws Exception {
        String fileName = "BrainFlow-" + BoardIds.from_code(dataExtractor.getBoardId()) + "-" + new SimpleDateFormat("yyyyMMddHHmm'.xlsx'").format(new Date());
        ExcelExporter exporter = new ExcelExporter();
        exporter.generateExcelFile(fileName, dataExtractor, chartDescriptors);
    }

}
