package com.network;

import com.data_management.DataStorage;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * PatientWebSocketClient connects to a WebSocket server to receive real-time patient data,
 * parses incoming messages, and updates the DataStorage with the parsed information.
 */
public class PatientWebSocketClient extends WebSocketClient {

    private final DataStorage dataStorage;

    /**
     * Constructs a PatientWebSocketClient with the specified WebSocket server URI and DataStorage instance.
     *
     * @param serverUri   the URI of the WebSocket server to connect to
     * @param dataStorage the DataStorage instance used to store parsed patient data
     * @throws URISyntaxException if the provided server URI is invalid
     */
    public PatientWebSocketClient(String serverUri, DataStorage dataStorage) throws URISyntaxException {
        super(new URI(serverUri));
        this.dataStorage = dataStorage;
    }

    /**
     * Called when the WebSocket connection is successfully established.
     *
     * @param handshake the handshake data received from the server
     */
    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to WebSocket server");
    }

    /**
     * Handles incoming messages from the WebSocket server.
     * Parses JSON-formatted messages and updates the DataStorage with extracted patient data.
     *
     * @param message the message received from the WebSocket server
     */
    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
        try {
            JsonObject json = com.google.gson.JsonParser.parseString(message).getAsJsonObject();

            if (!json.has("patientId") || !json.has("measurementValue") ||
                    !json.has("recordType") || !json.has("timestamp")) {
                System.err.println("Invalid message: Missing required fields");
                return;
            }

            int patientId = json.get("patientId").getAsInt();
            double measurementValue = json.get("measurementValue").getAsDouble();
            String recordType = json.get("recordType").getAsString();
            long timestamp = json.get("timestamp").getAsLong();

            dataStorage.addPatientData(patientId, measurementValue, recordType, timestamp);

        } catch (Exception e) {
            System.err.println("Failed to parse/store message: " + e.getMessage());
        }
    }

    /**
     * Called when the WebSocket connection is closed.
     * If the closure was initiated remotely, the client will attempt to reconnect with exponential backoff.
     *
     * @param code   the status code indicating the reason for closure
     * @param reason the reason for closure
     * @param remote whether the closure was initiated by the remote host
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket connection closed: " + reason);
        if (remote) {
            System.out.println("Attempting to reconnect...");
            reconnectWithBackoff(5); // Try every 5 seconds
        }
    }

    /**
     * Handles WebSocket errors by printing the exception message to standard error.
     *
     * @param ex the exception representing the error
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }

    /**
     * Attempts to reconnect to the WebSocket server at regular intervals using a simple backoff strategy.
     *
     * @param seconds the number of seconds to wait between reconnection attempts
     */
    private void reconnectWithBackoff(int seconds) {
        new Thread(() -> {
            while (!this.isOpen()) {
                try {
                    Thread.sleep(seconds * 1000L);
                    this.reconnect(); // built-in method in WebSocketClient
                    System.out.println("Reconnecting...");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Reconnection failed: " + e.getMessage());
                }
            }
        }).start();
    }
}
