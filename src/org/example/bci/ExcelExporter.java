package org.example.bci;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

public final class ExcelExporter {
    final static String SAMPLE_TITLE = "Sample";
    final static String VALUE_TITLE = "Value";

    public void createXSSFSheetWithLineChart(XSSFSheet dataSheet, XSSFSheet chartSheet, String chartTitle, String catAxisTitle, String yAxisTitle, XSSFCell[] headers, CellRangeAddress dataRange, XSSFClientAnchor anchor, int[] columns, ChartTypes chartType) {

        XSSFDrawing drawing = chartSheet.createDrawingPatriarch();

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(chartTitle);
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle(catAxisTitle);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle(yAxisTitle);

        XDDFDataSource<String> xs = XDDFDataSourcesFactory.fromStringCellRange(dataSheet, new CellRangeAddress(
                dataRange.getFirstRow(), dataRange.getLastRow(),
                dataRange.getFirstColumn(), dataRange.getFirstColumn()));

        XDDFChartData data = chart.createData(chartType, bottomAxis, leftAxis);

        for(int column : columns) {
            XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(dataSheet, new CellRangeAddress(
                    dataRange.getFirstRow(), dataRange.getLastRow(),
                    dataRange.getFirstColumn() + column, dataRange.getFirstColumn() + column));

            XDDFChartData.Series series = data.addSeries(xs, ys);
            series.setTitle(headers[column].getStringCellValue(), new CellReference(headers[column]));

            switch (chartType) {
                case LINE -> {
                    ((XDDFLineChartData.Series) series).setSmooth(true);
                    ((XDDFLineChartData.Series) series).setMarkerStyle(MarkerStyle.NONE);
                }
                case LINE3D -> {
                    ((XDDFLine3DChartData.Series) series).setSmooth(true);
                    ((XDDFLine3DChartData.Series) series).setMarkerStyle(MarkerStyle.NONE);

                }
            }
        }

        chart.plot(data);
    }

    public void streamDataIntoSXSSFWorkbook(SXSSFSheet sheet, DataExtractor dataExtractor) {

        // Create all data rows.
        for (int sampleIndex = 0; sampleIndex < dataExtractor.getSampleCount(); sampleIndex++) {
            sheet.createRow(sampleIndex + 1).createCell(0).setCellValue(sampleIndex + 1);
        }

        int cellIndex = 0;
        for (double[] dataRow : dataExtractor.getData()) {
            // Populate data rows and cells.
            int rowIndex = 0;
            for (double value : dataRow) {
                sheet.getRow(rowIndex++ + 1).createCell(cellIndex + 1).setCellValue(value);
            }
            cellIndex++;
        }
    }

    public void generateExcelFile(String fileName, DataExtractor dataExtractor, int sampleCount, String[] dataLabels) throws Exception {

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet dataSheet = wb.createSheet("Data");

        // Create first header row.
        XSSFCell[] headers = createHeader(dataSheet, dataLabels);

        List<String> labels = Arrays.stream(dataLabels).toList();

        String prefix = "Frontal";
        XSSFSheet chartSheetFrontal = wb.createSheet(prefix);
        makeChart(dataSheet, chartSheetFrontal, prefix, SAMPLE_TITLE, VALUE_TITLE, headers, findColumnsStartingWith(labels, prefix), sampleCount, false);

        prefix = "Central";
        XSSFSheet chartSheetCentral = wb.createSheet(prefix);
        makeChart(dataSheet, chartSheetCentral, prefix, SAMPLE_TITLE, VALUE_TITLE, headers, findColumnsStartingWith(labels, prefix), sampleCount, true);

        prefix = "Gyro";
        XSSFSheet chartSheetGyro = wb.createSheet(prefix);
        makeChart(dataSheet, chartSheetGyro, prefix, SAMPLE_TITLE, VALUE_TITLE, headers, findColumnsStartingWith(labels, prefix), sampleCount, false);

        SXSSFWorkbook sWb = new SXSSFWorkbook(wb);
        SXSSFSheet sSheet = sWb.getSheetAt(0);
        streamDataIntoSXSSFWorkbook(sSheet, dataExtractor);

        FileOutputStream fileOut = new FileOutputStream(fileName);
        sWb.write(fileOut);
        fileOut.close();
        sWb.dispose();
    }


    public XSSFCell[] createHeader(XSSFSheet dataSheet, String[] dataLabels) {
        XSSFRow row = dataSheet.createRow(0);
        XSSFCell cell = row.createCell(0);
        cell.setCellValue("Period");

        // Setup headers.
        XSSFCell[] headers = new XSSFCell[dataLabels.length];

        for (int i = 0; i < dataLabels.length; i++) {
            cell = row.createCell(i + 1);
            cell.setCellValue(dataLabels[i]);
            headers[i] = cell;
        }

        return headers;
    }

    public int[] findColumnsStartingWith(List<String> labels, String prefix) {

        return IntStream.range(0, labels.size())
                .filter(i -> labels.get(i).startsWith(prefix))
                .boxed()
                .mapToInt(Integer::intValue)
                .toArray();

    }
    public void makeChart(XSSFSheet dataSheet, XSSFSheet chartSheet, String chartTitle, String catAxisTitle, String yAxisTitle, XSSFCell[] headers, int[] columns, int numSamples, boolean chartType3D) {

        // Create line chart.
        createXSSFSheetWithLineChart(dataSheet, chartSheet, chartTitle, catAxisTitle, yAxisTitle, headers,
                new CellRangeAddress(1,  numSamples, 0, 2),
                new XSSFClientAnchor(0, 0, 0, 0, 3, 1, 35, 50), columns, chartType3D ? ChartTypes.LINE3D : ChartTypes.LINE
        );

    }
}