package data_management;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import org.mockito.Mock;
import org.mockito.Spy;

class AlertGeneratorTest {

    private DataStorage dataStorage;

    @Spy
    private AlertGenerator alertGenerator;

    @Mock
    private Patient patient;

    private List<PatientRecord> patientRecords;

    @BeforeEach
    void setUp() {
        dataStorage = mock(DataStorage.class);
        patient = mock(Patient.class);
        alertGenerator = spy(new AlertGenerator(dataStorage));  // Create a spy of the AlertGenerator
        patientRecords = new ArrayList<>();
    }

    @Test
    void testCheckBloodPressureAlertsCriticalSystolic() {
        // Set up patient records with critical systolic blood pressure values
        PatientRecord systolicHigh = new PatientRecord(1, 185.0, "Systolic", 1714376789050L);
        PatientRecord systolicLow = new PatientRecord(1, 85.0, "Systolic", 1714376789051L);
        patientRecords.add(systolicHigh);
        patientRecords.add(systolicLow);

        when(patient.getRecords(0, Long.MAX_VALUE)).thenReturn(patientRecords);

        // Trigger the alert check
        alertGenerator.checkBloodPressureAlerts(patient);

        // Verify if the alert was triggered for critical systolic
        verify(alertGenerator, times(2)).triggerAlert(any(Alert.class));  // Expecting two alerts
    }

    @Test
    void testCheckBloodSaturationAlertsLowSaturation() {
        // Set up patient records with low blood oxygen saturation
        PatientRecord lowSaturation = new PatientRecord(1, 90.0, "BloodSaturation", 1714376789050L);
        patientRecords.add(lowSaturation);

        when(patient.getRecords(0, Long.MAX_VALUE)).thenReturn(patientRecords);

        // Check for low oxygen saturation
        alertGenerator.checkBloodSaturationAlerts(patient);

        // Verify if the alert was triggered for low saturation
        verify(alertGenerator, times(1)).triggerAlert(argThat(alert -> alert.getCondition().equals("Low Oxygen Saturation")));
    }

    @Test
    void testCheckHypotensiveHypoxemia() {
        // Set up patient records with low blood pressure and low oxygen saturation
        PatientRecord systolic = new PatientRecord(1, 85.0, "Systolic", 1714376789050L);
        PatientRecord saturation = new PatientRecord(1, 88.0, "BloodSaturation", 1714376789051L);
        patientRecords.add(systolic);
        patientRecords.add(saturation);

        when(patient.getRecords(0, Long.MAX_VALUE)).thenReturn(patientRecords);

        // Check for Hypotensive Hypoxemia
        alertGenerator.checkHypotensiveHypoxemia(patient);

        // Verify if the Hypotensive Hypoxemia alert was triggered
        verify(alertGenerator, times(1)).triggerAlert(argThat(alert -> alert.getCondition().equals("Hypotensive Hypoxemia Alert")));
    }

    @Test
    void testCheckECGAlertsAbnormalPeak() {
        // Provide enough ECG records to exceed the window threshold
        for (int i = 0; i < 10; i++) {
            patientRecords.add(new PatientRecord(1, 100.0, "ECG", 1714376789000L + i));
        }

        // Add one abnormal peak
        patientRecords.add(new PatientRecord(1, 200.0, "ECG", 1714376789010L));

        when(patient.getRecords(0, Long.MAX_VALUE)).thenReturn(patientRecords);

        alertGenerator.checkECGAlerts(patient);

        verify(alertGenerator, atLeastOnce()).triggerAlert(argThat(alert ->
                alert.getCondition().equals("Abnormal ECG Peak")
        ));
    }


    @Test
    void testCheckManualAlerts() {
        // Set up patient records with a manual alert
        PatientRecord manualAlert = new PatientRecord(1, 0.0, "ManualAlert", 1714376789050L);
        patientRecords.add(manualAlert);

        when(patient.getRecords(0, Long.MAX_VALUE)).thenReturn(patientRecords);

        // Check for manual alert
        alertGenerator.checkManualAlerts(patient);

        // Verify if the manual alert was triggered
        verify(alertGenerator, times(1)).triggerAlert(argThat(alert -> alert.getCondition().equals("Manual Alert Triggered")));
    }

    @Test
    void testNoAlertWhenConditionsNotMet() {
        // Set up patient records where no condition is met
        PatientRecord normalSystolic = new PatientRecord(1, 120.0, "Systolic", 1714376789050L);
        PatientRecord normalSaturation = new PatientRecord(1, 98.0, "BloodSaturation", 1714376789051L);
        patientRecords.add(normalSystolic);
        patientRecords.add(normalSaturation);

        when(patient.getRecords(0, Long.MAX_VALUE)).thenReturn(patientRecords);

        // Check for alerts
        alertGenerator.evaluateData(patient);

        // Verify that no alerts were triggered
        verify(alertGenerator, times(0)).triggerAlert(any(Alert.class));
    }


}
