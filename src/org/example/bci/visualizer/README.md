# BCIVisualizer
A simple app that visualizes EEG data from a Brain-Computer Interface (BCI) device.
- Extract BCI data from EEG devices, export data to MS Excel and creates charts by filtering selected data columns.
- Uses BrainFlow API to extract data from the BCI device.
- Uses Apache POI library to create Excel files and charts.

[BCIVisualizer.java](src/org/example/bci/visualizer/BCIVisualizer.java)
The main entrypoint. 
- Gets BCI data from a DataExtractor.
- Define the charts to be created.
- Exports Excel data and channel filtered charts.


[ChartDescriptor.java](src/org/example/bci/visualizer/ChartDescriptor.java)
Describes a chart to be created.

[DataExtractor.java](src/org/example/bci/visualizer/DataExtractor.java)
Extracts data from a BCI device.

[ExcelExporter.java](src/org/example/bci/visualizer/ExcelExporter.java)
Export data and charts to an Excel file.

[ParamParser.java](src/org/example/bci/visualizer/ParamParser.java)
Parses the command line parameters.

### Prerequisites
Fork or clone the app
Download, unzip and configure BrainFlowJars.zip to be an external library in your IDE (developer tool/platform).

### Starting the app
Start the app by running the main method in BCIVisualizer.java, with its default settings, using the synthetic board.

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

### Data Streaming
Waiting for data...which is read as a two-dimensional double data[Channel][Datapoints] array. And the channel definitions/metadata are in the configuration dump listed above.

Yes data is here:
[198.0, 199.0, 200.0, 201.0, 202.0, 203.0, 204.0, 205.0, 206.0, 207.0, 208.0, 209.0, 210.0, 211.0, 212.0, 213.0, 214.0, 215.0, 216.0, 217.0, 218.0, 219.0, 220.0, 221.0, 222.0, 223.0, 224.0, 225.0, 226.0, 227.0]
[13.593835755987348, 11.774385356374767, 9.99999999999821, 8.180843444616427, 6.432437963015419, 5.049803594788741, 3.163087569098021, 1.4412914756153015, 0.3898982782237166, -1.128021263483106, -2.2313524424261395, ...

### Excel Export
The app exports the data to an Excel file, and creates charts by filtering selected data columns via regex, so one chart can dynamically include channel columns by name pattern.
![BCI Data](https://learningisliving.dk/wp-content/uploads/2024/12/bci-excel-data.png)
### Excel Charts
Each chart gets its own sheet in the Excel file, and the charts are named by the chart title.

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
Example chart:
![BCIVisualizer](https://learningisliving.dk/wp-content/uploads/2024/12/bci-excel-chart.png)

### Dependencies
The code depends up on different jar files that can be found here [BrainFlowJars.zip](https://drive.google.com/file/d/124RQcCQjArB9xW4oa_1Qri9ljCv8JVuO/view?usp=drive_link), and related as external libraries.

This ZIP file contains all the jar files for BrainFlow and Excel integration APIs to execute the demo.
Unzip the file and reference it, as the BrainFlowJars library folder, in your development IDE.

This solution is developed in IntelliJ 2023.1.1 (Ultimate Edition).

## Developed by
Ronni Kahalani 2024.

