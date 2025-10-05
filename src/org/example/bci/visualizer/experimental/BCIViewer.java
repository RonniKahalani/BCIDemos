package org.example.bci.visualizer.experimental;

import brainflow.BoardIds;
import brainflow.BoardShim;
import brainflow.BrainFlowError;
import brainflow.BrainFlowInputParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.bci.visualizer.properties.PropertyLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A simple Java Swing application that connects to a BrainFlow-compatible BCI device,
 * streams EEG data, and visualizes it in real-time.
 */
public class BCIViewer extends JFrame implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(BCIViewer.class);
    private static final int BOARD_ID = BoardIds.SYNTHETIC_BOARD.get_code(); // Change to your board ID
    private static final int BUFFER_SIZE = 250; // ~1 second at 250Hz
    private static final int UPDATE_INTERVAL_MS = 100; // Update every 100ms

    private BoardShim boardShim;
    private volatile boolean isStreaming = false;
    private final double[] eegBuffer = new double[BUFFER_SIZE];
    private int bufferIndex = 0;
    private JPanel chartPanel;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;
    private JComboBox<String> channelComboBox;
    private int selectedChannel = 0;
    private JLabel dataLabel;

    private JLabel labelLineX1;
    private JLabel labelLineX2;
    private JLabel labelLineY1;
    private JLabel labelLineY2;

    /**
     * Constructor to set up the GUI and BrainFlow session.
     */
    public BCIViewer() {
        createUI();
    }

    /**
     * Initializes the UI components.
     */
    private void createUI() {

        logger.info("Starting BCI Viewer");

        // UI is created in the constructor
        setTitle("BrainFlow BCI Data Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel topPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Disconnected", SwingConstants.CENTER);
        topPanel.add(statusLabel);

        // Channel selection
        java.util.List<String> eegComboBoxLabels = new ArrayList<>();
        Map<String, String> eegLabelMap = PropertyLoader.get("data-labels.properties");
        try {
            java.util.List<String> eegNames = List.of(BoardShim.get_eeg_names(BOARD_ID));// Ensure channels are loaded

            eegComboBoxLabels = eegNames.stream()
                    .map(channelId -> eegLabelMap.containsKey(channelId) ? eegLabelMap.get(channelId) + " (" + channelId + ")" : channelId)
                    .toList();
        } catch (BrainFlowError e) {
            statusLabel.setText("Error loading channels: " + e.getMessage());
            logger.fatal(e);
        }

        channelComboBox = new JComboBox<>(eegComboBoxLabels.toArray(new String[0]));
        channelComboBox.setEditable(false);
        channelComboBox.addActionListener(e -> selectedChannel = channelComboBox.getSelectedIndex());
        topPanel.add(channelComboBox, BorderLayout.EAST);
        add(topPanel);

        // Chart panel
        chartPanel = new ChartPanel();
        add(chartPanel);

        JPanel infoPanel = new JPanel(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(createLineInfoUI());
        add(infoPanel);

        // Control buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(createControlUI(), BorderLayout.CENTER);
        bottomPanel.add(createDataUI(), BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        initializeBoard();
        pack();
        setVisible(true);
    }

    /**
     * Creates the line information UI.
     *
     * @return the line info UI panel
     */
    private JPanel createLineInfoUI() {
        // Line info values for line coordinates
        labelLineX1 = new JLabel("0");
        labelLineX2 = new JLabel("0");
        labelLineY1 = new JLabel("0");
        labelLineY2 = new JLabel("0");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel x1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        x1Panel.add(new JLabel("X1: "));
        x1Panel.add(labelLineX1);
        panel.add(x1Panel);

        JPanel y1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        y1Panel.add(new JLabel("Y1: "));
        y1Panel.add(labelLineY1);
        panel.add(y1Panel);

        JPanel x2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        x2Panel.add(new JLabel("X2: "));
        x2Panel.add(labelLineX2);
        panel.add(x2Panel);

        JPanel y2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        y2Panel.add(new JLabel("Y2: "));
        y2Panel.add(labelLineY2);
        panel.add(y2Panel);

        return panel;
    }

    /**
     * Initializes the BrainFlow board session.
     */
    private void initializeBoard() {
        try {
            // Initialize BrainFlow
            BoardShim.enable_dev_board_logger();
            BrainFlowInputParams params = new BrainFlowInputParams();
            // For real boards, set params.serial_port, params.mac_address, etc.
            // e.g., params.serial_port = "/dev/ttyUSB0";

            boardShim = new BoardShim(BOARD_ID, params);
            boardShim.prepare_session();
            statusLabel.setText("Session prepared. Click Start to begin streaming.");
        } catch (BrainFlowError | IOException | ReflectiveOperationException e) {
            statusLabel.setText("Error preparing session: " + e.getMessage());
            logger.fatal(e);
        }
    }

    /**
     * Creates the data display UI.
     *
     * @return the data UI panel
     */
    private JPanel createDataUI() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dataLabel = new JLabel("");
        panel.add(dataLabel);
        return panel;
    }

    /**
     * Creates the control buttons UI.
     *
     * @return the control UI panel
     */
    private JPanel createControlUI() {
        JPanel panel = new JPanel();
        startButton = new JButton("Start Streaming");
        stopButton = new JButton("Stop Streaming");
        stopButton.setEnabled(false);
        panel.add(startButton);
        panel.add(stopButton);

        // Action listeners
        startButton.addActionListener(e -> startStreaming());
        stopButton.addActionListener(e -> stopStreaming());

        return panel;
    }

    /**
     * Starts the data streaming and visualization.
     */
    private void startStreaming() {
        if (!isStreaming) {
            try {
                boardShim.start_stream(450000, ""); // Default stream options
                isStreaming = true;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                statusLabel.setText("Streaming...");

                // Streaming thread
                new Thread(() -> {
                    while (isStreaming) {
                        try {
                            if (boardShim.get_board_data_count() > 0) {
                                double[][] data = boardShim.get_board_data(1); // Get latest packet

                                int eegChannelIndex = BoardShim.get_eeg_channels(BOARD_ID)[selectedChannel];
                                double eegValue = data[eegChannelIndex][0]; // 0-indexed in array

                                // Update buffer (circular)
                                synchronized (this) {
                                    eegBuffer[bufferIndex] = eegValue;
                                    bufferIndex = (bufferIndex + 1) % BUFFER_SIZE;
                                }

                                // Queue UI update
                                SwingUtilities.invokeLater(chartPanel::repaint);
                            }
                            Thread.sleep(UPDATE_INTERVAL_MS);
                        } catch (BrainFlowError | InterruptedException e) {
                            logger.fatal(e);
                            stopStreaming();
                        }
                    }
                }).start();
            } catch (BrainFlowError e) {
                statusLabel.setText("Error starting stream: " + e.getMessage());
                logger.fatal(e);
            }
        }
    }

    /**
     * Stops the data streaming and visualization.
     */
    private void stopStreaming() {
        if (isStreaming) {
            isStreaming = false;
            try {
                boardShim.stop_stream();
                statusLabel.setText("Stream stopped.");
            } catch (BrainFlowError e) {
                logger.fatal(e);
            }
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            chartPanel.repaint();
        }
    }

    /**
     * Cleans up the BrainFlow session.
     */
    @Override
    public void close() {
        if (boardShim != null) {
            try {
                if (isStreaming) {
                    boardShim.stop_stream();
                }
                boardShim.release_session();
                statusLabel.setText("Session released.");
            } catch (BrainFlowError e) {
                throw new RuntimeException("Error releasing session: " + e.getMessage(), e);
            } finally {
                boardShim = null;
            }
        }
    }

    /**
     * Custom JPanel to draw the EEG data.
     */
    class ChartPanel extends JPanel {

        /**
         * Paints the EEG data on the panel.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int height;
            int halfHeight;
            int width;

            // Draw EEG data
            synchronized (BCIViewer.this) {
                if (bufferIndex == 0) return; // No data yet

                int visiblePoints = Math.min(bufferIndex, BUFFER_SIZE);
                double[] displayBuffer = new double[visiblePoints];
                System.arraycopy(eegBuffer, 0, displayBuffer, 0, visiblePoints);

                dataLabel.setText(String.format("Latest EEG Value: %.2f μV", displayBuffer[visiblePoints - 1]));

                height = getHeight() - 40;
                halfHeight = height / 2;
                width = getWidth();

                // Scale data to fit panel (simple min-max scaling)
                double min = Arrays.stream(displayBuffer).min().orElse(0);
                double max = Arrays.stream(displayBuffer).max().orElse(0);
                double rangeHeightFactor = getRangeHeightFactor(min, max, height);
                int pointWidth = width / (visiblePoints - 1);

                g2d.setColor(Color.BLACK);
                g2d.drawLine(0, halfHeight, width, halfHeight); // Zero line

                g2d.setColor(Color.BLUE);

                for (int i = 0; i < visiblePoints - 1; i++) {
                    double y1 = height - ((displayBuffer[i] - min) / rangeHeightFactor);
                    double y2 = height - ((displayBuffer[i + 1] - min) / rangeHeightFactor);
                    double x1 = i * pointWidth;
                    double x2 = (i + 1) * pointWidth;
                    g2d.draw(new Line2D.Double(x1, y1, x2, y2));

                    labelLineX1.setText(String.valueOf((int)x1));
                    labelLineX2.setText(String.valueOf((int)x2));
                    labelLineY1.setText(String.valueOf((int)y1));
                    labelLineY2.setText(String.valueOf((int)y2));
                }
            }

            // Labels
            g2d.setColor(Color.GRAY);
            g2d.drawString("EEG Channel " + channelComboBox.getSelectedItem() + " (μV)", 10, 15);
            g2d.drawString("Time →", width - 50, height - 10);
        }

        /**
         * Calculates the factor to scale the EEG range to the panel height.
         *
         * @param min    minimum EEG value
         * @param max    maximum EEG value
         * @param height height of the panel
         * @return scaling factor
         */
        private double getRangeHeightFactor(double min, double max, int height) {
            double range = max - min;
            if (range == 0) range = 1; // Avoid division by zero
            return range / (height * 0.8);
        }

        /**
         * Preferred size of the chart panel.
         */
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(800, 400);
        }
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(BCIViewer::new);
    }
}
