package org.example.bci;

import brainflow.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Extracts data from a BCI device.
 */
public class DataExtractor {

    final static int BUFFER_SIZE = 3600;
    final static int SAMPLE_COUNT = 100;
    final static long WAIT_MILLIS = 5000;

    private final HashMap<String, String> dataDescriptions = new HashMap<>();
    private String[] dataLabels = null;
    private double[][] data = null;
    private BoardDescr boardDescr;
    private BrainFlowInputParams params;
    private BoardIds boardId;

    private int bufferSize;

    private long waitMillis;

    private int sampleCount;

    /**
     * Default constructor using the synthetic board device with default properties.
     *
     * @throws BrainFlowError
     */
    public DataExtractor() throws BrainFlowError {
        this(BoardIds.SYNTHETIC_BOARD, new BrainFlowInputParams(), BUFFER_SIZE, WAIT_MILLIS, SAMPLE_COUNT);
    }

    /**
     * Constructor for a given board device, params, buffer size, wait time and sample count.
     *
     * @param boardId
     * @param params
     * @param bufferSize
     * @param waitMillis
     * @param sampleCount
     * @throws BrainFlowError
     */
    public DataExtractor(BoardIds boardId, BrainFlowInputParams params, int bufferSize, long waitMillis, int sampleCount) throws BrainFlowError {

        setBufferSize(bufferSize);
        setWaitMillis(waitMillis);
        setSampleCount(sampleCount);
        setParams(params);
        setBoardId(boardId);
        setBoardDescr(BoardShim.get_board_descr(BoardDescr.class, boardId));
        dumpDescriptor(boardDescr);
        initializeDataLabels();
    }

    /**
     * Returns the buffer size.
     *
     * @return size of the buffer.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets the buffer size.
     *
     * @param bufferSize
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Returns the wait time.
     *
     * @return wait time in millis.
     */
    public long getWaitMillis() {
        return waitMillis;
    }

    /**
     * Sets the wait time in millis.
     *
     * @param waitMillis
     */
    public void setWaitMillis(long waitMillis) { this.waitMillis = waitMillis;}

    /**
     * Returns the sample count.
     *
     * @return sample count.
     */
    public int getSampleCount() {
        return sampleCount;
    }

    /**
     * Sets the sample count.
     *
     * @param sampleCount
     */
    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }

    /**
     * Initializes the data labels.
     */
    private void initializeDataLabels() {

        dataDescriptions.put("Fz", "Frontal midline");
        dataDescriptions.put("C3", "Central left side");
        dataDescriptions.put("Cz", "Central midline");
        dataDescriptions.put("C4", "Central right side");
        dataDescriptions.put("Pz", "Parietal midline");
        dataDescriptions.put("PO7", "Parieto-Occipital left side");
        dataDescriptions.put("Oz", "Occipital midline");
        dataDescriptions.put("PO8", "Parieto-Occipital right side");
        dataDescriptions.put("F1", "Frontal coronal outer left midline");
        dataDescriptions.put("F2", "Frontal coronal left midline");
        dataDescriptions.put("F3", "Frontal coronal right midline");
        dataDescriptions.put("F4", "Frontal coronal outer right midline");
        dataDescriptions.put("F5", "Frontal lateral level 3");
        dataDescriptions.put("F6", "Frontal lateral level 3");
        dataDescriptions.put("F7", "Frontal left near temple");
        dataDescriptions.put("F8", "Frontal right near temple");

        dataLabels = new String[boardDescr.num_rows];

        int nameIndex = 0;
        String[] eegNames = boardDescr.eeg_names.split(",");
        for (int row : boardDescr.eeg_channels) {
            String channelName = eegNames[nameIndex++];
            String eegTitle = dataDescriptions.get(channelName);
            String fullName = eegTitle == null ? channelName : eegTitle + " (" + channelName + ")";
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

    /**
     * Returns the data.
     *
     * @return data.
     */
    public double[][] getData() {
        return data;
    }

    /**
     * Returns the data descriptions.
     *
     * @return data descriptions.
     */
    public HashMap<String,String> getDataDescriptions() {
        return dataDescriptions;
    }

    /**
     * Returns the data labels.
     *
     * @return data labels.
     */
    public String[] getDataLabels() {
        return dataLabels;
    }

    /**
     * Returns the board descriptors.
     *
     * @return board descriptors.
     */
    public BoardDescr getBoardDescr() {
        return boardDescr;
    }

    /**
     * Sets the board descriptors.
     *
     * @param boardDescr
     */
    public void setBoardDescr(BoardDescr boardDescr) {
        this.boardDescr = boardDescr;
    }

    /**
     * Returns the params.
     *
     * @return params.
     */
    public BrainFlowInputParams getParams() {
        return params;
    }

    /**
     * Sets the params.
     *
     * @param params
     */
    public void setParams(BrainFlowInputParams params) {
        this.params = params;
    }

    /**
     * Returns the board id.
     * @return boardId.
     */
    public BoardIds getBoardId() {
        return boardId;
    }

    /**
     * Sets the board id.
     *
     * @param boardId
     */
    public void setBoardId(BoardIds boardId) {
        this.boardId = boardId;
    }

    /**
     * Extracts a specific number of data samples.
     *
     * @param sampleCount
     * @throws Exception
     */
    public void extractData(int sampleCount) throws Exception {
        this.sampleCount = sampleCount;
        extractData();
    }

    /**
     * Extracts data, based on the internal sample count.
     *
     * @throws Exception
     */
    public void extractData() throws Exception {
        BoardShim.enable_board_logger();

        BoardShim board_shim = new BoardShim(boardId, getParams());
        board_shim.prepare_session();

        board_shim.start_stream(getBufferSize());
        BoardShim.log_message(LogLevels.LEVEL_INFO, "Waiting %sms for data...".formatted(getWaitMillis()));
        Thread.sleep(getWaitMillis());
        board_shim.stop_stream();

        System.out.println(board_shim.get_board_data_count());
        int num_rows = BoardShim.get_num_rows(boardId);
        data = board_shim.get_current_board_data(getSampleCount());

        for (int i = 0; i < num_rows; ++i) {
            System.out.println(Arrays.toString(data[i]));
        }

        board_shim.release_session();

    }

    /**
     * Filters data signals.
     *
     * @throws BrainFlowError
     */
    public void signalFiltering() throws BrainFlowError {

        int samplingRate = BoardShim.get_sampling_rate(boardId);
        int[] eeg_channels = BoardShim.get_eeg_channels(boardId);

        int i;
        for(i = 0; i < eeg_channels.length; ++i) {
            switch (i) {
                case 0 ->
                        DataFilter.perform_lowpass(data[eeg_channels[i]], samplingRate, 50.0, 4, FilterTypes.BESSEL, 0.0);
                case 1 ->
                        DataFilter.perform_highpass(data[eeg_channels[i]], samplingRate, 3.0, 4, FilterTypes.BUTTERWORTH, 0.0);
                case 2 ->
                        DataFilter.perform_bandpass(data[eeg_channels[i]], samplingRate, 3.0, 50.0, 4, FilterTypes.CHEBYSHEV_TYPE_1, 1.0);
                case 3 ->
                        DataFilter.perform_bandstop(data[eeg_channels[i]], samplingRate, 48.0, 52.0, 4, FilterTypes.CHEBYSHEV_TYPE_1, 1.0);
                default -> DataFilter.remove_environmental_noise(data[eeg_channels[i]], samplingRate, NoiseTypes.FIFTY);
            }
        }

    }

    /**
     * Downsamples data for a given period and operation.
     *
     * @param period
     * @param operation
     * @throws BrainFlowError
     */
    public void downsample(int period, AggOperations operation) throws BrainFlowError {

        int[] eeg_channels = BoardShim.get_eeg_channels(boardId);
        System.out.println("Downsampling:");
        for(int i = 0; i < eeg_channels.length; ++i) {
            System.out.println("Original data:");
            System.out.println(Arrays.toString(data[i]));
            double[] downsampled_data = DataFilter.perform_downsampling(data[eeg_channels[i]], period, operation);
            System.out.println("Downsampled data:");
            System.out.println(Arrays.toString(downsampled_data));
        }

    }

    /**
     * Creates data labels.
     *
     * @param values
     * @param labelPrefix
     */
    public void createDataLabels(List<Integer> values, String labelPrefix) {
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                int index = values.get(i);
                if (dataLabels[index] == null) {
                    dataLabels[index] = labelPrefix + " " + (i + 1);
                }
            }
        }
    }

    /**
     * Dumps a list of channels.
     *
     * @param channels
     * @return channels as string.
     */
    public String dumpChannels(List<Integer> channels) {
        return (channels != null && !channels.isEmpty()) ? channels.toString() : "None";
    }

    /**
     * Dumps board descriptors.
     *
     * @param boardDescr
     */
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