package org.example.bci;

import brainflow.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Extracts data from a BCI device.
 */
public class DataExtractor {

    final static int BUFFER_SIZE = 1024;
    final static int SAMPLE_COUNT = BUFFER_SIZE;
    final static long WAIT_MILLIS = 5000;

    private final HashMap<String, String> dataDescriptions = new HashMap<>();
    private String[] dataLabels = null;
    private double[][] data = null;
    private BoardDescr boardDescr;
    private BrainFlowInputParams params;
    private int boardId;

    private int bufferSize;

    private long waitMillis;

    private int sampleCount;

    private int samplingRate;

    private double oxygenLevel;

    private double heartRate;

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
    public DataExtractor(int boardId, BrainFlowInputParams params, int bufferSize, long waitMillis, int sampleCount) throws BrainFlowError {

        setBufferSize(bufferSize);
        setWaitMillis(waitMillis);
        setSampleCount(sampleCount);
        setParams(params);
        setBoardId(boardId);
        setSamplingRate(BoardShim.get_sampling_rate(boardId));
        setBoardDescr(BoardShim.get_board_descr(BoardDescr.class, boardId));
        dumpDescriptor(boardDescr);
        initializeDataLabels();
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public int getSamplingRate() {
        return samplingRate;
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
            String fullName = eegTitle == null ? channelName : channelName + " - " +eegTitle;
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
    public int getBoardId() {
        return boardId;
    }

    /**
     * Sets the board id.
     *
     * @param boardId
     */
    public void setBoardId(int boardId) {
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

        extractPPGValues();
    }

    /**
     * Extracts different PPG values.
     *
     * @throws BrainFlowError
     */
    private void extractPPGValues() throws BrainFlowError {
        int[] ppgChannels = BoardShim.get_ppg_channels(boardId);
        double[] ppgIr = data[ppgChannels[1]];
        double[] ppgRed = data[ppgChannels[0]];

        extractOxygenLevel(ppgIr, ppgRed, samplingRate);
        extractHeartRate(ppgIr, ppgRed, samplingRate, BUFFER_SIZE);

        System.out.println("Oxygen level: " + getOxygenLevel());
        System.out.println("Heart rate: " + getHeartRate());
    }

    /**
     * Extracts the oxygen level from the data.
     *
     * @param ppgIr
     * @param ppgRed
     * @param samplingRate
     * @throws BrainFlowError
     */
    private void extractOxygenLevel(double[] ppgIr, double[] ppgRed,int samplingRate) throws BrainFlowError {
        oxygenLevel = DataFilter.get_oxygen_level(ppgIr, ppgRed, samplingRate);
    }

    /**
     * Extracts the heart rate from the data.
     *
     * @param ppgIr
     * @param ppgRed
     * @param samplingRate
     * @param fftSize
     * @throws BrainFlowError
     */
    private void extractHeartRate(double[] ppgIr, double[] ppgRed, int samplingRate, int fftSize) throws BrainFlowError {
        heartRate = DataFilter.get_heart_rate(ppgIr, ppgRed, samplingRate, fftSize);
    }

    /**
     * Low pass signal filter.
     *
     * @param dataChannel
     * @param samplingRate
     * @param cutOff
     * @param order
     * @param filterType
     * @param ripple
     * @throws BrainFlowError
     */
    public void filterLowPass(double[] dataChannel, int samplingRate, double cutOff, int order, FilterTypes filterType, double ripple) throws BrainFlowError {
        DataFilter.perform_lowpass(dataChannel, samplingRate, cutOff, order, filterType, ripple);
    }

    /**
     * High pass signal filter.
     *
     * @param dataChannel
     * @param samplingRate
     * @param cutOff
     * @param order
     * @param filterType
     * @param ripple
     * @throws BrainFlowError
     */
    public void filterHighPass(double[] dataChannel, int samplingRate, double cutOff, int order, FilterTypes filterType, double ripple) throws BrainFlowError {
        DataFilter.perform_highpass(dataChannel, samplingRate, cutOff, order, filterType, ripple);
    }

    /**
     * Bans pass signal filter.
     * @param dataChannel
     * @param samplingRate
     * @param startFrequency
     * @param stopFrequency
     * @param order
     * @param filterType
     * @param ripple
     * @throws BrainFlowError
     */
    public void filterBandPass(double[] dataChannel, int samplingRate, double startFrequency, double stopFrequency, int order, FilterTypes filterType, double ripple) throws BrainFlowError {
        DataFilter.perform_bandpass(dataChannel, samplingRate, startFrequency, stopFrequency, order, filterType, ripple);
    }

    /**
     * Bans stop signal filter.
     *
     * @param dataChannel
     * @param samplingRate
     * @param startFrequency
     * @param stopFrequency
     * @param order
     * @param filterType
     * @param ripple
     * @throws BrainFlowError
     */

    public void filterBandStop(double[] dataChannel, int samplingRate, double startFrequency, double stopFrequency, int order, FilterTypes filterType, double ripple) throws BrainFlowError {
        DataFilter.perform_bandstop(dataChannel, samplingRate, startFrequency, stopFrequency, order, filterType, ripple);
    }

    /**
     * Remove environmental noise signal filter.
     *
     * @param dataChannel
     * @param samplingRate
     * @param noiseType
     * @throws BrainFlowError
     */
    public void filterRemoveEnvironmentalNoise(double[] dataChannel, int samplingRate, NoiseTypes noiseType) throws BrainFlowError {
        DataFilter.remove_environmental_noise(dataChannel, samplingRate, noiseType);
    }



    /**
     * Downsamples data for a given period and operation.
     *
     * @param dataChannel
     * @param period
     * @param operation
     * @throws BrainFlowError
     */
    public double[] downsample(double[] dataChannel, int period, AggOperations operation) throws BrainFlowError {
        return DataFilter.perform_downsampling(dataChannel, period, operation);
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

    public double getOxygenLevel() {
        return oxygenLevel;
    }

    public void setOxygenLevel(double oxygenLevel) {
        this.oxygenLevel = oxygenLevel;
    }

    public double getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(double heartRate) {
        this.heartRate = heartRate;
    }
}