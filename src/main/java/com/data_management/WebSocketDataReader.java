package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.io.IOException;

/**
 * Connects to a WebSocket server and reads real-time patient data.
 */
public class WebSocketDataReader implements DataReader {

    private final URI serverUri;

    public WebSocketDataReader(String serverUri) {
        this.serverUri = URI.create(serverUri);
    }

    @Override
    public void readData(DataStorage storage) throws IOException {
        WebSocketClient client = new WebSocketClient(serverUri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("Connected to WebSocket server.");
            }

            @Override
            public void onMessage(String message) {
                try {
                    parseAndAdd(message, storage);
                } catch (Exception e) {
                    System.err.println("Invalid message received: " + message);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("WebSocket closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };

        client.connect();

        // Keep the thread alive so it continues to receive messages
        try {
            while (!client.isOpen()) {
                Thread.sleep(100); // wait until connection is established
            }
            System.out.println("Listening for real-time data...");
            // Keep listening — in a real application you’d use a better lifecycle strategy
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            throw new IOException("WebSocket interrupted", e);
        }
    }

    private void parseAndAdd(String line, DataStorage storage) {
        // Format: patientId,timestamp,label,data
        String[] parts = line.split(",");
        if (parts.length != 4) throw new IllegalArgumentException("Invalid data format");

        int patientId = Integer.parseInt(parts[0].trim());
        long timestamp = Long.parseLong(parts[1].trim());
        String label = parts[2].trim();
        double value = Double.parseDouble(parts[3].trim());

        storage.addPatientData(patientId, value, label, timestamp);
    }
}
