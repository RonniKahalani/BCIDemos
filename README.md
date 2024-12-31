# BCIDemos

This repo includes examples of BCI - Brain-Computer Interface apps, using the BrainFlow API.


## BCIVisualizer
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
Images of the app:
![BCIVisualizer](https://learningisliving.dk/wp-content/uploads/2024/12/bci-excel-chart.png)

### Dependencies
The code depends up on different jar files that can be found here [BrainFlowJars.zip](https://drive.google.com/file/d/124RQcCQjArB9xW4oa_1Qri9ljCv8JVuO/view?usp=drive_link), and related as external libraries.

This ZIP file contains all the jar files for BrainFlow and Excel integration APIs to execute the demo.
Unzip the file and reference it, as the BrainFlowJars library folder, in your development IDE.

This solution is developed in IntelliJ 2023.1.1 (Ultimate Edition).

## Developed by
Ronni Kahalani 2024.

