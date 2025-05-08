package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * A TCP-based implementation of the {@link OutputStrategy} interface.
 * <p>
 * This class starts a TCP server on a specified port, accepts one client connection,
 * and streams data output to that client in CSV format:
 * {@code patientId,timestamp,label,data}.
 * </p>
 *
 * Example message: {@code 1234,1715160000000,ECG,0.92}
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

    /**
     * Constructs a new {@code TcpOutputStrategy} that listens for a single client
     * connection on the specified port.
     *
     * @param port The TCP port on which the server listens for incoming connections.
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends patient data to the connected TCP client, formatted as a CSV line.
     *
     * @param patientId The ID of the patient.
     * @param timestamp The timestamp of the measurement.
     * @param label     The label of the signal (e.g., "ECG", "BP").
     * @param data      The actual signal data value.
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
