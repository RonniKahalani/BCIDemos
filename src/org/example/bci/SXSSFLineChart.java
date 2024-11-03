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

public final class SXSSFLineChart {

    final static String SAMPLE_TITLE = "Sample";
    final static String VALUE_TITLE = "Value";

    static void createXSSFSheetWithLineChart(XSSFSheet dataSheet, XSSFSheet chartSheet, String chartTitle, String catAxisTitle, String yAxisTitle, XSSFCell[] headers, CellRangeAddress dataRange, XSSFClientAnchor anchor, int[] columns) {

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

        XDDFChartData data = chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

        for(int column : columns) {
            XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(dataSheet, new CellRangeAddress(
                    dataRange.getFirstRow(), dataRange.getLastRow(),
                    dataRange.getFirstColumn() + column, dataRange.getFirstColumn() + column));

            XDDFChartData.Series series = data.addSeries(xs, ys);
            series.setTitle(headers[column].getStringCellValue(), new CellReference(headers[column]));
            ((XDDFLineChartData.Series) series).setSmooth(true);
            ((XDDFLineChartData.Series) series).setMarkerStyle(MarkerStyle.NONE);

        }

        chart.plot(data);
    }

    static void streamDataIntoSXSSFWorkbook(SXSSFSheet sheet, DataExtractor dataExtractor) throws Exception {

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

    public static void main(String[] args) throws Exception {

        DataExtractor dataExtractor = new DataExtractor();
        dataExtractor.extractData();

        int numSamples = dataExtractor.getSampleCount();
        String[] dataLabels = dataExtractor.getDataLabels();

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet dataSheet = wb.createSheet("Data");

        // Create title for first row and first cell.
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


        List<String> labels = Arrays.stream(dataLabels).toList();

        XSSFSheet chartSheetFrontal = wb.createSheet("Frontal");
        makeChart(dataSheet, chartSheetFrontal, chartSheetFrontal.getSheetName(), SAMPLE_TITLE, VALUE_TITLE, headers, findColumnsStartingWith(labels, chartSheetFrontal.getSheetName()), numSamples);

        XSSFSheet chartSheetCentral = wb.createSheet("Central");
        makeChart(dataSheet, chartSheetCentral, chartSheetCentral.getSheetName(), SAMPLE_TITLE, VALUE_TITLE, headers, findColumnsStartingWith(labels, chartSheetCentral.getSheetName()), numSamples);

        XSSFSheet chartSheetGyro = wb.createSheet("Gyro");
        makeChart(dataSheet, chartSheetGyro, chartSheetGyro.getSheetName(), SAMPLE_TITLE, VALUE_TITLE, headers, findColumnsStartingWith(labels, chartSheetGyro.getSheetName()), numSamples);


        SXSSFWorkbook sWb = new SXSSFWorkbook(wb);
        SXSSFSheet sSheet = sWb.getSheetAt(0);
        streamDataIntoSXSSFWorkbook(sSheet, dataExtractor);

        String fileName = "BrainFlow-" + dataExtractor.getBoardId() + "-" + new SimpleDateFormat("yyyyMMddHHmm'.xlsx'").format(new Date());
        FileOutputStream fileOut = new FileOutputStream(fileName);
        sWb.write(fileOut);
        fileOut.close();
        sWb.dispose();
    }


    public static int[] findColumnsStartingWith(List<String> labels, String prefix) {

        return IntStream.range(0, labels.size())
                .filter(i -> labels.get(i).startsWith(prefix))
                .boxed()
                .mapToInt(Integer::intValue)
                .toArray();

    }
    public static void makeChart(XSSFSheet dataSheet, XSSFSheet chartSheet, String chartTitle, String catAxisTitle, String yAxisTitle, XSSFCell[] headers, int[] columns, int numSamples) {



        // Create line chart.
        createXSSFSheetWithLineChart(dataSheet, chartSheet, chartTitle, catAxisTitle, yAxisTitle, headers,
                new CellRangeAddress(1,  numSamples, 0, 2),
                new XSSFClientAnchor(0, 0, 0, 0, 3, 1, 20, 1 + 20), columns
        );

    }
}