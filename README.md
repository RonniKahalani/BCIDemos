# BCIDemos

This repo includes examples of BCI - Brain-Computer Interface apps, using the BrainFlow API.
![Data Sheet](doc/BCIVizualizer-data.png)    

## BCIVisualizer
A simple app that visualizes EEG data from a Brain-Computer Interface (BCI) device.
- Extract BCI data from EEG devices, export data to MS Excel and creates charts by filtering selected data columns.
- Uses BrainFlow API to extract data from the BCI device.
- Uses Apache POI library to create Excel files and charts.

![Data Sheet](doc/BCIVizualizer-sheet.png)
![Frontal](doc/BCIVizualizer-sheets-1.png)
![Central](doc/BCIVizualizer-sheets-2.png)
![Occipital](doc/BCIVizualizer-sheets-3.png)

<pre>
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
</pre>

[BCIVisualizer](src/org/example/bci/visualizer/README.md)

## Developed by
Ronni Kahalani 2024.

