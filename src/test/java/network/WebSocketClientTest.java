// File: WebSocketClientTest.java
package network;

import com.data_management.DataStorage;
import com.network.PatientWebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.mockito.Mockito.*;

public class WebSocketClientTest {

    private DataStorage mockDataStorage;
    private PatientWebSocketClient client;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        mockDataStorage = mock(DataStorage.class);
        client = new PatientWebSocketClient("ws://localhost:8080", mockDataStorage);
    }

    @Test
    public void testValidMessageHandling() {
        String validMessage = "{\"patientId\":1,\"measurementValue\":98.6,\"recordType\":\"Temperature\",\"timestamp\":1685000000000}";
        client.onMessage(validMessage);
        verify(mockDataStorage, times(1)).addPatientData(1, 98.6, "Temperature", 1685000000000L);
    }

    @Test
    public void testInvalidMessageMissingFields() {
        String invalidMessage = "{\"patientId\":1}";
        client.onMessage(invalidMessage);
        verify(mockDataStorage, never()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }

    @Test
    public void testCorruptedJsonMessage() {
        String corruptedMessage = "not_a_json";
        client.onMessage(corruptedMessage);
        verify(mockDataStorage, never()).addPatientData(anyInt(), anyDouble(), anyString(), anyLong());
    }
}



