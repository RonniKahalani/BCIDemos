package org.example.bci.visualizer;

import brainflow.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.bci.visualizer.properties.PropertyLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Extracts data from a BCI device.
 */
public class DataExtractor {

    // This buffer size should be at least 1000, to enable more advanced features and accurate oxygen level and heart rate.
    final static int BUFFER_SIZE = 1000;
    final static int SAMPLE_COUNT = BUFFER_SIZE;
    final static long WAIT_MILLIS = 5000;
    private static final Logger log = LogManager.getLogger(DataExtractor.class);

    private Map<String, String> dataDescriptions;
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
     * @param boardId     the board id.
     * @param params      the BrainFlowInputParams.
     * @param bufferSize  in samples.
     * @param waitMillis  in millis.
     * @param sampleCount number of samples to extract.
     * @throws BrainFlowError from the BrainFlow API.
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

    /**
     * Sets the sampling rate.
     *
     * @param samplingRate in Hz.
     */
    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    /**
     * Returns the sampling rate.
     *
     * @return sampling rate in Hz.
     */
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
     * @param bufferSize in samples.
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
     * @param waitMillis in millis.
     */
    public void setWaitMillis(long waitMillis) {
        this.waitMillis = waitMillis;
    }

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
     * @param sampleCount number of samples.
     */
    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }

    /**
     * Initializes the data labels.
     */
    private void initializeDataLabels() {
        dataDescriptions = PropertyLoader.get("data-labels.properties");
        dataLabels = new String[boardDescr.num_rows];

        int nameIndex = 0;
        String[] eegNames = boardDescr.eeg_names.split(",");
        for (int row : boardDescr.eeg_channels) {
            String channelName = eegNames[nameIndex++];
            String eegTitle = dataDescriptions.get(channelName);
            String fullName = eegTitle == null ? channelName : channelName + " - " + eegTitle;
            StringBuilder value = new StringBuilder(fullName + " eeg");

            if (boardDescr.eog_channels.contains(row)) {
                value.append("/eog");
            }
            if (boardDescr.emg_channels.contains(row)) {
                value.append("/emg");
            }

            dataLabels[row] = value.toString();
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
    public Map<String, String> getDataDescriptions() {
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
     * @param params the BrainFlowInputParams.
     */
    public void setParams(BrainFlowInputParams params) {
        this.params = params;
    }

    /**
     * Returns the board id.
     *
     * @return boardId.
     */
    public int getBoardId() {
        return boardId;
    }

    /**
     * Sets the board id.
     *
     * @param boardId the board id.
     */
    public void setBoardId(int boardId) {
        this.boardId = boardId;
    }

    /**
     * Extracts a specific number of data samples.
     *
     * @param sampleCount number of samples to extract.
     * @throws Exception from the BrainFlow API.
     */
    public void extractData(int sampleCount) throws Exception {
        setSampleCount(sampleCount);
        extractData();
    }

    /**
     * Extracts data, based on the internal sample count.
     *
     * @throws Exception from the BrainFlow API.
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
     * @throws BrainFlowError from the BrainFlow API.
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
     * @param ppgIr        photoplethysmography infrared data.
     * @param ppgRed       photoplethysmography red data.
     * @param samplingRate in Hz.
     */
    private void extractOxygenLevel(double[] ppgIr, double[] ppgRed, int samplingRate) {

        try {
            oxygenLevel = DataFilter.get_oxygen_level(ppgIr, ppgRed, samplingRate);
        } catch (BrainFlowError e) {
            if (BUFFER_SIZE < 1024) {
                log.error("Buffer size is less than 1024, oxygen level might be inaccurate. Try setting buffer size to 1024.");
            } else {
                log.error("e: ", e);
            }
        }
    }

    /**
     * Extracts the heart rate from the data.
     *
     * @param ppgIr        photoplethysmography infrared data.
     * @param ppgRed       photoplethysmography red data.
     * @param samplingRate in Hz.
     * @param fftSize      FFT size, should be power of 2.
     * @throws BrainFlowError from the BrainFlow API.
     */
    private void extractHeartRate(double[] ppgIr, double[] ppgRed, int samplingRate, int fftSize) throws BrainFlowError {

        try {
            heartRate = DataFilter.get_heart_rate(ppgIr, ppgRed, samplingRate, fftSize);
        } catch (BrainFlowError e) {
            if (BUFFER_SIZE < 1024) {
                log.error("Buffer size is less than 1024, heart rate might be inaccurate. Try setting buffer size to 1024.");
            } else {
                log.error("e: ", e);
            }
        }
    }

    /**
     * Low pass signal filter.
     *
     * @param dataChannel  data channel to filter.
     * @param samplingRate in Hz.
     * @param cutOff       cut off-frequency in Hz.
     * @param order        order of the filter.
     * @param filterType   type of the filter.
     * @param ripple       ripple for the filter.
     * @throws BrainFlowError from the BrainFlow API.
     */
    public void filterLowPass(double[] dataChannel, int samplingRate, double cutOff, int order, FilterTypes filterType, double ripple) throws BrainFlowError {
        DataFilter.perform_lowpass(dataChannel, samplingRate, cutOff, order, filterType, ripple);
    }

    /**
     * High pass signal filter.
     *
     * @param dataChannel  data channel to filter.
     * @param samplingRate in Hz.
     * @param cutOff       cut off frequency in Hz.
     * @param order        order of the filter.
     * @param filterType   type of the filter.
     * @param ripple       ripple for the filter.
     * @throws BrainFlowError from the BrainFlow API.
     */
    public void filterHighPass(double[] dataChannel, int samplingRate, double cutOff, int order, FilterTypes filterType, double ripple) throws BrainFlowError {
        DataFilter.perform_highpass(dataChannel, samplingRate, cutOff, order, filterType, ripple);
    }

    /**
     * Bans pass signal filter.
     *
     * @param dataChannel    data channel to filter.
     * @param samplingRate   in Hz.
     * @param startFrequency the frequency to start the band pass filter.
     * @param stopFrequency  the frequency to stop the band pass filter.
     * @param order          order of the filter.
     * @param filterType     type of the filter.
     * @param ripple         ripple for the filter.
     * @throws BrainFlowError from the BrainFlow API.
     */
    public void filterBandPass(double[] dataChannel, int samplingRate, double startFrequency, double stopFrequency, int order, FilterTypes filterType, double ripple) throws BrainFlowError {
        DataFilter.perform_bandpass(dataChannel, samplingRate, startFrequency, stopFrequency, order, filterType, ripple);
    }

    /**
     * Bans stop signal filter.
     *
     * @param dataChannel    data channel to filter.
     * @param samplingRate   in Hz.
     * @param startFrequency the frequency to start the band stop filter.
     * @param stopFrequency  the frequency to stop the band stop filter.
     * @param order          order of the filter.
     * @param filterType     type of the filter.
     * @param ripple         ripple for the filter.
     * @throws BrainFlowError from the BrainFlow API.
     */

    public void filterBandStop(double[] dataChannel, int samplingRate, double startFrequency, double stopFrequency, int order, FilterTypes filterType, double ripple) throws BrainFlowError {
        DataFilter.perform_bandstop(dataChannel, samplingRate, startFrequency, stopFrequency, order, filterType, ripple);
    }

    /**
     * Remove environmental noise signal filter.
     *
     * @param dataChannel  data channel to filter.
     * @param samplingRate in Hz.
     * @param noiseType    type of the noise to remove.
     * @throws BrainFlowError from the BrainFlow API.
     */
    public void filterRemoveEnvironmentalNoise(double[] dataChannel, int samplingRate, NoiseTypes noiseType) throws BrainFlowError {
        DataFilter.remove_environmental_noise(dataChannel, samplingRate, noiseType);
    }


    /**
     * Downsamples data for a given period and operation.
     *
     * @param dataChannel data channel to downsample.
     * @param period      downsampling period.
     * @param operation   aggregation operation.
     * @throws BrainFlowError from the BrainFlow API.
     */
    public double[] downsample(double[] dataChannel, int period, AggOperations operation) throws BrainFlowError {
        return DataFilter.perform_downsampling(dataChannel, period, operation);
    }

    /**
     * Creates data labels.
     *
     * @param values      list of channel indices.
     * @param labelPrefix the label prefix.
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
     * @param channels list of channels.
     * @return channels as string.
     */
    public String dumpChannels(List<Integer> channels) {
        return (channels != null && !channels.isEmpty()) ? channels.toString() : "None";
    }

    /**
     * Dumps board descriptors.
     *
     * @param boardDescr the board descriptors.
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

    /**
     * Returns the oxygen level.
     *
     * @return oxygen level.
     */
    public double getOxygenLevel() {
        return oxygenLevel;
    }

    /**
     * Sets the oxygen level.
     *
     * @param oxygenLevel oxygen level.
     */
    public void setOxygenLevel(double oxygenLevel) {
        this.oxygenLevel = oxygenLevel;
    }

    /**
     * Returns the heart rate.
     *
     * @return heart rate.
     */
    public double getHeartRate() {
        return heartRate;
    }

    /**
     * Sets the heart rate.
     *
     * @param heartRate heart rate.
     */
    public void setHeartRate(double heartRate) {
        this.heartRate = heartRate;
    }
}