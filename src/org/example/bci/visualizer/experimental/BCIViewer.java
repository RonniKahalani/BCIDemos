package org.example.bci.visualizer.experimental;

import brainflow.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.Arrays;

/**
 * A simple Java Swing application that connects to a BrainFlow-compatible BCI device,
 * streams EEG data, and visualizes it in real-time.
 */
public class BCIViewer extends JFrame implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(BCIViewer.class);
    private static final int BOARD_ID = BoardIds.SYNTHETIC_BOARD.get_code(); // Change to your board ID
    private static final int EEG_CHANNEL = 3; // First EEG channel (1-indexed)
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
    private JList<String> channelList;
    private int selectedChannel = 0;

    /**
     * Constructor to set up the GUI and BrainFlow session.
     */
    public BCIViewer() throws BrainFlowError {
        createUI();
    }

    /**
     * Initializes the UI components.
     */
    private void createUI() throws BrainFlowError {

        logger.info("Starting BCI Viewer");

        // UI is created in the constructor
        setTitle("BrainFlow BCI Data Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setExtendedState(JFrame.MAXIMIZED_BOTH);


        // Status label
        statusLabel = new JLabel("Disconnected", SwingConstants.CENTER);
        add(statusLabel, BorderLayout.NORTH);

        // Channel list
        channelList = new JList<>(new String[]{"Channel 1", "Channel 2", "Channel 3", "Channel 4", "Channel 5", "Channel 6", "Channel 7", "Channel 8"});
        channelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        channelList.setSelectedIndex(EEG_CHANNEL - 1);
        add(new JScrollPane(channelList), BorderLayout.WEST);
        channelList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedChannel = channelList.getSelectedIndex();
                if (selectedChannel != -1) {
                    // Update EEG channel (1-indexed)
                    // Note: In a real application, you would need to handle this change properly
                    // For simplicity, we just log it here
                    logger.info("Selected channel: " + (selectedChannel + 1));
                }
            }
        });
        add(new JScrollPane(channelList), BorderLayout.WEST);

        // Control buttons
        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start Streaming");
        stopButton = new JButton("Stop Streaming");
        stopButton.setEnabled(false);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Chart panel
        chartPanel = new ChartPanel();
        add(chartPanel, BorderLayout.CENTER);

        // Action listeners
        startButton.addActionListener(e -> startStreaming());
        stopButton.addActionListener(e -> stopStreaming());

        // Initialize BrainFlow
        BoardShim.enable_dev_board_logger();
        BrainFlowInputParams params = new BrainFlowInputParams();
        // For real boards, set params.serial_port, params.mac_address, etc.
        // e.g., params.serial_port = "/dev/ttyUSB0";

        try {
            boardShim = new BoardShim(BOARD_ID, params);
            boardShim.prepare_session();
            statusLabel.setText("Session prepared. Click Start to begin streaming.");
        } catch (BrainFlowError | IOException | ReflectiveOperationException e) {
            statusLabel.setText("Error preparing session: " + e.getMessage());
            logger.fatal(e);
        }

        setVisible(true);
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
                                double eegValue = data[EEG_CHANNEL - 1 + selectedChannel][0]; // 0-indexed in array

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
            int width;

            // Draw EEG data
            synchronized (BCIViewer.this) {
                if (bufferIndex == 0) return; // No data yet

                int visiblePoints = Math.min(bufferIndex, BUFFER_SIZE);
                double[] displayBuffer = new double[visiblePoints];
                System.arraycopy(eegBuffer, BUFFER_SIZE - visiblePoints, displayBuffer, 0, visiblePoints);

                // Scale data to fit panel (simple min-max scaling)
                double min = Arrays.stream(displayBuffer).min().orElse(0);
                double max = Arrays.stream(displayBuffer).max().orElse(0);
                double range = max - min;
                if (range == 0) range = 1; // Avoid division by zero

                height = getHeight() - 40;
                width = getWidth();
                int pointWidth = width / (visiblePoints - 1);

                g2d.setColor(Color.BLACK);
                g2d.drawLine(0, height / 2, width, height / 2); // Zero line

                g2d.setColor(Color.BLUE);
                for (int i = 0; i < visiblePoints - 1; i++) {
                    double y1 = (double) height / 2 - ((displayBuffer[i] - min) / range * height * 0.8);
                    double y2 = (double) height / 2 - ((displayBuffer[i + 1] - min) / range * height * 0.8);
                    double x1 = i * pointWidth;
                    double x2 = (i + 1) * pointWidth;
                    g2d.draw(new Line2D.Double(x1, y1, x2, y2));
                    statusLabel.setText("coords: (" + x1 + ", " + y1 + ")(" + x2 + ", " + y2 + ")");
                }
            }

            // Labels
            g2d.setColor(Color.GRAY);
            g2d.drawString("EEG Channel " + EEG_CHANNEL + " (μV)", 10, 15);
            g2d.drawString("Time →", width - 50, height - 10);
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
    public static void main(String[] args) throws BrainFlowError {
        SwingUtilities.invokeLater(() -> {
            try {
                new BCIViewer();
            } catch (BrainFlowError e) {
                throw new RuntimeException(e);
            }
        });
    }
}
