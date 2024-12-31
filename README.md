# BCIDemos

This repo includes examples of BCI - Brain-Computer Interface apps, using the BrainFlow API.

Features

BCIVisualizer - A simple app that visualizes EEG data from a Brain-Computer Interface (BCI) device.
- Extract BCI data from EEG devices, export data to MS Excel and creates charts by filtering selected data columns.


## BCIVisualizer
[BCIVisualizer.java](src/org/example/bci/visualizer/BCIVisualizer.java)
Visualizes a BCI data extract into an Excel file.

[ChartDescriptor.java](src/org/example/bci/visualizer/ChartDescriptor.java)
Describes a chart to be created.

[DataExtractor.java](src/org/example/bci/visualizer/DataExtractor.java)
Extracts data from a BCI device.

[ExcelExporter.java](src/org/example/bci/visualizer/ExcelExporter.java)
Export data and charts to an Excel file.

[ParamParser.java](src/org/example/bci/visualizer/ParamParser.java)
Parses the command line parameters.


### Dependencies
The code depends up on different jar files that can be [found here](https://drive.google.com/file/d/124RQcCQjArB9xW4oa_1Qri9ljCv8JVuO/view?usp=drive_link), and related as external libraries.

This ZIP file contains all the jar files for BrainFlow and Excel integration APIs to execute the demo.

Unzip the file and reference it, as the BrainFlowJars library folder, in your development IDE.

This solution is developed in IntelliJ 2023.1.1 (Ultimate Edition).

## Developed by
Ronni Kahalani 2024.

