package org.example.bci.visualizer;

import brainflow.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.Arrays;

public class BCIViewer extends JFrame implements AutoCloseable {
    private static final int BOARD_ID = BoardIds.SYNTHETIC_BOARD.get_code(); // Change to your board ID
    private static final int EEG_CHANNEL = 3; // First EEG channel (1-indexed)
    private static final int BUFFER_SIZE = 250; // ~1 second at 250Hz
    private static final int UPDATE_INTERVAL_MS = 100; // Update every 100ms

    private BoardShim boardShim;
    private volatile boolean isStreaming = false;
    private final double[] eegBuffer = new double[BUFFER_SIZE];
    private int bufferIndex = 0;
    private final JPanel chartPanel;
    private final JButton startButton;
    private final JButton stopButton;
    private final JLabel statusLabel;

    public BCIViewer() throws BrainFlowError {
        setTitle("BrainFlow BCI Data Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 600);

        // Status label
        statusLabel = new JLabel("Disconnected", SwingConstants.CENTER);
        add(statusLabel, BorderLayout.NORTH);

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
            e.printStackTrace();
        }

        setVisible(true);
    }

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
                                double eegValue = data[EEG_CHANNEL - 1][0]; // 0-indexed in array

                                // Update buffer (circular)
                                synchronized (this) {
                                    eegBuffer[bufferIndex] = eegValue;
                                    bufferIndex = (bufferIndex + 1) % BUFFER_SIZE;
                                }

                                // Queue UI update
                                SwingUtilities.invokeLater(chartPanel::repaint);
                            }
                            Thread.sleep(UPDATE_INTERVAL_MS);
                        } catch (BrainFlowError | InterruptedException ex) {
                            ex.printStackTrace();
                            stopStreaming();
                        }
                    }
                }).start();
            } catch (BrainFlowError e) {
                statusLabel.setText("Error starting stream: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void stopStreaming() {
        if (isStreaming) {
            isStreaming = false;
            try {
                boardShim.stop_stream();
                statusLabel.setText("Stream stopped.");
            } catch (BrainFlowError e) {
                e.printStackTrace();
            }
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            chartPanel.repaint();
        }
    }

    @Override
    public void close() throws Exception {
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

    // Inner class for the chart panel
    class ChartPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int height = getHeight();
            int width = getWidth();

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
                    double y1 = height / 2 - ((displayBuffer[i] - min) / range * height * 0.8);
                    double y2 = height / 2 - ((displayBuffer[i + 1] - min) / range * height * 0.8);
                    g2d.draw(new Line2D.Double(i * pointWidth, y1, (i + 1) * pointWidth, y2));
                }
            }

            // Labels
            g2d.setColor(Color.GRAY);
            g2d.drawString("EEG Channel " + EEG_CHANNEL + " (μV)", 10, 15);
            g2d.drawString("Time →", width - 50, height - 10);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(800, 400);
        }
    }

    public static void main(String[] args) throws BrainFlowError {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new BCIViewer();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
