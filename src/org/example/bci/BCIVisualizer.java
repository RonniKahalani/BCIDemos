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


    public static void main(String[] args) throws Exception {

        DataExtractor dataExtractor = new DataExtractor();
        dataExtractor.extractData();

        ExcelExporter exporter = new ExcelExporter();

        List<ChartDescriptor> chartDescriptors = new ArrayList<>();

        String prefix = "Frontal";
        chartDescriptors.add( new ChartDescriptor(prefix, "(?i)^" + prefix + ".*$", true, prefix, SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
        prefix = "Central";
        chartDescriptors.add( new ChartDescriptor(prefix, "(?i)^" + prefix + ".*$", false, prefix, SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
        prefix = "Gyro";
        chartDescriptors.add( new ChartDescriptor(prefix, "(?i)^" + prefix + ".*$", true, prefix, SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));

        String fileName = "BrainFlow-" + dataExtractor.getBoardId() + "-" + new SimpleDateFormat("yyyyMMddHHmm'.xlsx'").format(new Date());
        exporter.generateExcelFile(fileName, dataExtractor, dataExtractor.getSampleCount(), dataExtractor.getDataLabels(), chartDescriptors);

    }
}
