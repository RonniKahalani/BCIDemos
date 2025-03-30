# BCIVisualizer
This app visualizes EEG data from a Brain-Computer Interface (BCI) device.
- Uses BrainFlow Java API to extract data from a standard BCI device.
- Exports BCI data to Excel spreadsheets.
- Creates charts by filtering selected channel data columns.
- Uses Apache POI to manage the Excel integration.

### Data Sheet
All channel sample data is imported to the first sheet, in a new Excel file.
Each column is a specific channel (sensor spot on the head/body, or some control data column) and all its rows are data samples over time.
The requsted/used number of samples/rows is configuable in the app.

![Data Sheet](/doc/BCIVizualizer-data.png) 

### Dynamic chart sheets
You can define an "infinite" number of charts, via a simple line of code.

Regular Expressions are used to include columns in a chart.
A chart can filter on multiple regular expressions.

```Java
 private static void configureCharts() {

        String[] titles = {"Frontal", "Central", "Occipital", "Gyro"};
        chartDescriptors = new ArrayList<>();
        chartDescriptors.add( new ChartDescriptor(titles[0], List.of("(?i)^F.*$"), false, titles[0], SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
        chartDescriptors.add( new ChartDescriptor(titles[1], List.of("(?i)^C.*$"), false, titles[1], SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
        chartDescriptors.add( new ChartDescriptor(titles[2], List.of("(?i)^O.*$", "(?i)^PO.*$", "(?i)^Pz.*$"), false, titles[2], SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
        chartDescriptors.add( new ChartDescriptor(titles[3], List.of("(?i)^Gyro.*$"), false, titles[3], SAMPLE_TITLE, VALUE_TITLE, MarkerStyle.DOT));
    }
```

### Example Charts
Here are some chart screenshots of the other sheets.

![Frontal](/doc/BCIVizualizer-sheets-1.png)
![Central](/doc/BCIVizualizer-sheets-2.png)
![Occipital](/doc/BCIVizualizer-sheets-3.png)

### Dependencies
This app code depends up on different jar libraries/files found here [BrainFlowJars.zip](https://drive.google.com/file/d/124RQcCQjArB9xW4oa_1Qri9ljCv8JVuO/view?usp=drive_link).

This ZIP file contains all the jar files for BrainFlow, Excel integration and other APIs to execute the demo.
Unzip the file and reference it, as the BrainFlowJars library folder, in your cloned/forked development IDE project.

## Get it up and running
- Fork or clone this project app code.
- Download, unzip and configure the [dependency BrainFlowJars.zip](#dependencies) in your IDE.
- Start the app by running the main method in BCIVisualizer.java, with its default settings, using the synthetic board.

### Data Streaming
Data is read as a two-dimensional array.
```Java
double data[ channelIndex ][ sampleIndex ];
```

### Configuration Dump
The first thing you'll see, is a configuration dump from the board:

Name: Synthetic
Sampling rate: 250

Num rows: 32

EEG names: Fz,C3,Cz,C4,Pz,PO7,Oz,PO8,F5,F7,F3,F1,F2,F4,F6,F8

EEG channels: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16]

ACCEL channels: [17, 18, 19]

EDA channels: [23]

EOG channels: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16]

EMG channels: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16]

EXG channels: None

Gyro channels: [20, 21, 22]

PGG channels: [24, 25]

Temperature channels: [26]

Resistance channels: [27, 28]

Rotation channels: None

Other channels: None

Battery channel: 29

Marker channel: 31

Timestamp channel: 30

Package num channel: 0

## Classes
[BCIVisualizer.java](BCIVisualizer.java)
The main entrypoint. 
- Gets BCI data from a DataExtractor.
- Define the charts to be created.
- Exports Excel data and channel filtered charts.

[ChartDescriptor.java](ChartDescriptor.java)
Describes a chart to be created.

[DataExtractor.java](DataExtractor.java)
Extracts data from a BCI device.

[ExcelExporter.java](ExcelExporter.java)
Export data and charts to an Excel file.

[ParamParser.java](ParamParser.java)
Parses the command line parameters.
