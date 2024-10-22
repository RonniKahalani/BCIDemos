package org.example.bci;


import brainflow.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DataExtractor {

    private final HashMap<String, String> eegDescriptions = new HashMap<>();
    private String[] dataLabels = null;
    private double[][] data = null;
    private BoardDescr boardDescr = null;
    private BrainFlowInputParams params = null;
    private BoardIds boardId = null;

    private int bufferSize = 3600;

    private long waitMillis = 5000;

    private int numSamples = 30;

    public DataExtractor() throws BrainFlowError {
        this(3600, 5000, 30);
    }
    public DataExtractor(int bufferSize, long waitMillis, int numSamples) throws BrainFlowError {

        this.bufferSize = bufferSize;
        this.waitMillis = waitMillis;
        this.numSamples = numSamples;

        params = new BrainFlowInputParams();
        boardId = BoardIds.SYNTHETIC_BOARD;

        boardDescr = BoardShim.get_board_descr(BoardDescr.class, boardId);
        dumpDescriptor(boardDescr);
        initializeDataLabels();
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public long getWaitMillis() {
        return waitMillis;
    }

    public int getNumSamples() {
        return numSamples;
    }

    private void initializeDataLabels() {

        eegDescriptions.put("Fz", "Frontal midline");
        eegDescriptions.put("C3", "Central left side");
        eegDescriptions.put("Cz", "Central midline");
        eegDescriptions.put("C4", "Central right side");
        eegDescriptions.put("Pz", "Parietal midline");
        eegDescriptions.put("PO7", "Parieto-Occipital left side");
        eegDescriptions.put("Oz", "Occipital midline");
        eegDescriptions.put("PO8", "Parieto-Occipital right side");
        eegDescriptions.put("F1", "Frontal coronal outer left midline");
        eegDescriptions.put("F2", "Frontal coronal left midline");
        eegDescriptions.put("F3", "Frontal coronal right midline");
        eegDescriptions.put("F4", "Frontal coronal outer right midline");
        eegDescriptions.put("F5", "Frontal lateral level 3");
        eegDescriptions.put("F6", "Frontal lateral level 3");
        eegDescriptions.put("F7", "Frontal left near temple");
        eegDescriptions.put("F8", "Frontal right near temple");

        dataLabels = new String[boardDescr.num_rows + 1];

        int nameIndex = 0;
        String[] eegNames = boardDescr.eeg_names.split(",");
        for (int row : boardDescr.eeg_channels) {
            String channelName = eegNames[nameIndex++];
            String eegTitle = eegDescriptions.get(channelName);
            String fullName = eegTitle == null ? channelName : eegTitle + "(" + channelName + ")";
            String value = fullName + " eeg";
            if (boardDescr.eog_channels.contains(row)) {
                value += "/eog";
            }
            if (boardDescr.emg_channels.contains(row)) {
                value += "/emg";
            }

            dataLabels[row] = value;
        }

        createDataLabels(boardDescr.accel_channels, "Accel");
        createDataLabels(boardDescr.rotation_channels, "Rotation");
        createDataLabels(boardDescr.temperature_channels, "Temperature");
        createDataLabels(boardDescr.gyro_channels, "Gyro");
        createDataLabels(boardDescr.resistance_channels, "Resistance");
        createDataLabels(boardDescr.ppg_channels, "PGG");
        createDataLabels(boardDescr.exg_channels, "EXG");
        createDataLabels(boardDescr.eda_channels, "EDA");
        createDataLabels(boardDescr.other_channels, "Other");
        createDataLabels(boardDescr.eog_channels, "EOG");
        createDataLabels(boardDescr.emg_channels, "EMG");

        dataLabels[boardDescr.package_num_channel] = "Package";
        dataLabels[boardDescr.timestamp_channel] = "Timestamp";
        dataLabels[boardDescr.marker_channel] = "Marker";
        dataLabels[boardDescr.battery_channel] = "Battery";
    }

    public double[][] getData() {
        return data;
    }

    public HashMap<String,String> getEegDescriptions() {
        return eegDescriptions;
    }

    public String[] getDataLabels() {
        return dataLabels;
    }

    public BoardDescr getBoardDescr() {
        return boardDescr;
    }

    public BrainFlowInputParams getParams() {
        return params;
    }

    public BoardIds getBoardId() {
        return boardId;
    }

    public void extractData() throws Exception {
        BoardShim.enable_board_logger();

        BoardShim board_shim = new BoardShim(boardId, params);
        board_shim.prepare_session();

        board_shim.start_stream(bufferSize);
        BoardShim.log_message(LogLevels.LEVEL_INFO, "Start sleeping in the main thread");
        Thread.sleep(waitMillis);
        board_shim.stop_stream();

        System.out.println(board_shim.get_board_data_count());
        int num_rows = BoardShim.get_num_rows(boardId);
        data = board_shim.get_current_board_data(numSamples);

        for (int i = 0; i < num_rows; ++i) {
            System.out.println(Arrays.toString(data[i]));
        }

        board_shim.release_session();
    }

    public void createDataLabels(List<Integer> values, String labelPrefix) {
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                if (dataLabels[values.get(i)] == null) {
                    dataLabels[values.get(i)] = labelPrefix + " " + (i + 1);
                }
            }
        }
    }

    public String dumpChannels(List<Integer> channels) {
        return (channels != null && !channels.isEmpty()) ? channels.toString() : "None";
    }

    public void dumpDescriptor(BoardDescr boardDescr) {

        System.out.println("Name: " + boardDescr.name);
        System.out.println("Sampling rate: " + boardDescr.sampling_rate);
        System.out.println("Num rows: " + boardDescr.num_rows);
        System.out.println("EEG names: " + boardDescr.eeg_names);

        System.out.println("EEG channels: " + dumpChannels(boardDescr.eeg_channels));
        System.out.println("ACCEL channels: " + dumpChannels(boardDescr.accel_channels));
        System.out.println("EDA channels: " + dumpChannels(boardDescr.eda_channels));
        System.out.println("EOG channels: " + dumpChannels(boardDescr.eog_channels));
        System.out.println("EMG channels: " + dumpChannels(boardDescr.emg_channels));
        System.out.println("EXG channels: " + dumpChannels(boardDescr.exg_channels));
        System.out.println("Gyro channels: " + dumpChannels(boardDescr.gyro_channels));
        System.out.println("PGG channels: " + dumpChannels(boardDescr.ppg_channels));
        System.out.println("Temperature channels: " + dumpChannels(boardDescr.temperature_channels));
        System.out.println("Resistance channels: " + dumpChannels(boardDescr.resistance_channels));
        System.out.println("Rotation channels: " + dumpChannels(boardDescr.rotation_channels));
        System.out.println("Other channels: " + dumpChannels(boardDescr.other_channels));

        System.out.println("Battery channel: " + boardDescr.battery_channel);
        System.out.println("Marker channel: " + boardDescr.marker_channel);
        System.out.println("Timestamp channel: " + boardDescr.timestamp_channel);
        System.out.println("Package num channel: " + boardDescr.package_num_channel);

    }
}