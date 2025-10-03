package org.example.bci.visualizer;

import brainflow.BrainFlowInputParams;

/**
 * Parses the command line parameters.
 */
public class ParamParser {
    /**
     * Parses the command line parameters.
     *
     * @param args   command line arguments.
     * @param params BrainFlowInputParams to populate.
     * @return the board ID.
     */
    public static int parseParams(String[] args, BrainFlowInputParams params) {
        int boardId = -1;

        for (int i = 0; i < args.length; ++i) {
            String argId = args[i];
            String argValue = args[i + 1];

            switch (argId) {
                case "--ip-address" -> params.ip_address = argValue;
                case "--serial-port" -> params.serial_port = argValue;
                case "--ip-protocol" -> params.ip_protocol = Integer.parseInt(argValue);
                case "--other-info" -> params.other_info = argValue;
                case "--board-id" -> boardId = Integer.parseInt(argValue);
                case "--timeout" -> params.timeout = Integer.parseInt(argValue);
                case "--serial-number" -> params.serial_number = argValue;
                case "--file" -> params.file = argValue;
            }
        }

        return boardId;
    }
}
