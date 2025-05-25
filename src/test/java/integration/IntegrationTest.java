// File: IntegrationTest.java
package integration;

import com.data_management.*;
import com.network.PatientWebSocketClient;
import com.alerts.AlertGenerator;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    @Test
    public void testEndToEndFlow() throws URISyntaxException {
        DataStorage storage = new DataStorage();
        PatientWebSocketClient client = new PatientWebSocketClient("ws://localhost:8080", storage);

        String message = "{\"patientId\":2,\"measurementValue\":120.5,\"recordType\":\"BloodPressure\",\"timestamp\":1685000000000}";
        client.onMessage(message);

        assertNotNull(storage.getPatient(2));
        assertEquals(1, storage.getRecords(2, 0, System.currentTimeMillis()).size());

        AlertGenerator generator = new AlertGenerator(storage);
        generator.evaluateData(storage.getPatient(2));
    }
}
