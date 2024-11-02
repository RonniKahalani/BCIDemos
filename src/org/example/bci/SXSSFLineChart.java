package org.example.bci;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.streaming.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

public final class SXSSFLineChart {

    static void createXSSFSheetWithLineChart(XSSFSheet sheet, String chartTitle, String catAxisTitle, String yAxisTitle, XSSFCell[] headers, CellRangeAddress dataRange, XSSFClientAnchor anchor, int[] columns) {

        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(chartTitle);
        chart.setTitleOverlay(false);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle(catAxisTitle);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle(yAxisTitle);

        XDDFDataSource<String> xs = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(
                dataRange.getFirstRow(), dataRange.getLastRow(),
                dataRange.getFirstColumn(), dataRange.getFirstColumn()));

        XDDFChartData data = chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

        for(int column : columns) {
            XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(
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
        for (int sampleIndex = 0; sampleIndex < dataExtractor.getNumSamples(); sampleIndex++) {
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

        int numSamples = dataExtractor.getNumSamples();
        String[] dataLabels = dataExtractor.getDataLabels();

        final int DATA_START_ROW = 1;

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();

        // Create title for first row and first cell.
        XSSFRow row = sheet.createRow(0);
        XSSFCell cell = row.createCell(0);
        cell.setCellValue("Period");

        // Setup headers.
        XSSFCell[] headers = new XSSFCell[dataLabels.length];

        for (int i = 0; i < dataLabels.length; i++) {
            cell = row.createCell(i + 1);
            cell.setCellValue(dataLabels[i]);
            headers[i] = cell;
        }

        int[] columns = {5,10};
        // Create line chart.
        createXSSFSheetWithLineChart(sheet, "Brainflow", "Sample", "Value", headers,
                new CellRangeAddress(DATA_START_ROW, DATA_START_ROW + numSamples - 1, 0, 2),
                new XSSFClientAnchor(0, 0, 0, 0, 3, 1, 20, DATA_START_ROW + 20), columns
        );

        SXSSFWorkbook sWb = new SXSSFWorkbook(wb);
        SXSSFSheet sSheet = sWb.getSheetAt(0);
        streamDataIntoSXSSFWorkbook(sSheet, dataExtractor);

        String fileName = "brainflow-" + dataExtractor.getBoardId() + "-" + new SimpleDateFormat("yyyyMMddHHmm'.xlsx'").format(new Date());
        FileOutputStream fileOut = new FileOutputStream(fileName);
        sWb.write(fileOut);
        fileOut.close();
        sWb.dispose();
    }
}