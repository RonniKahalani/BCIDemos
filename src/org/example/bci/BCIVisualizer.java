package org.example.bci;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Visualizes a BCI data extract into an Excel file.
 */
public class BCIVisualizer {

    public static void main(String[] args) throws Exception {
        DataExtractor dataExtractor = new DataExtractor();
        dataExtractor.extractData();

        ExcelExporter exporter = new ExcelExporter();
        String fileName = "BrainFlow-" + dataExtractor.getBoardId() + "-" + new SimpleDateFormat("yyyyMMddHHmm'.xlsx'").format(new Date());
        exporter.generateExcelFile(fileName, dataExtractor, dataExtractor.getSampleCount(), dataExtractor.getDataLabels());

    }
}
