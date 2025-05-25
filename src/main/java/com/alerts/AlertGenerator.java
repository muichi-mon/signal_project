package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */

public class AlertGenerator {
    private DataStorage dataStorage;

    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the given patient's medical data and checks for all defined alert conditions,
     * including blood pressure, oxygen saturation, hypotensive hypoxemia, ECG anomalies, and manual alerts.
     *
     * @param patient the patient whose data will be evaluated for possible alert conditions
     */
    public void evaluateData(Patient patient) {
        checkBloodPressureAlerts(patient);
        checkBloodSaturationAlerts(patient);
        checkHypotensiveHypoxemia(patient);
        checkECGAlerts(patient);
        checkManualAlerts(patient);
    }


    /**
     * Triggers an alert by logging its details to the console. This method can be extended
     * in future to notify medical staff, store alerts persistently, or integrate with alert systems.
     *
     * @param alert the alert object containing patient ID, condition, and timestamp
     */
    public void triggerAlert(Alert alert) {
        System.out.println("ALERT: Patient " + alert.getPatientId()
                + " - Condition: " + alert.getCondition()
                + " @ " + alert.getTimestamp());
    }

    /**
     * Checks the patient's systolic and diastolic blood pressure records to detect:
     * - Critical threshold violations (systolic > 180 or < 90; diastolic > 120 or < 60)
     * - Consistent increasing or decreasing trends over three consecutive readings
     *
     * @param patient the patient whose blood pressure records will be evaluated
     */
    public void checkBloodPressureAlerts(Patient patient) {
        List<PatientRecord> records = patient.getRecords(0, Long.MAX_VALUE);

        List<PatientRecord> systolic = new ArrayList<>();
        List<PatientRecord> diastolic = new ArrayList<>();

        for (PatientRecord r : records) {
            if ("Systolic".equalsIgnoreCase(r.getRecordType())) {
                systolic.add(r);
                if (r.getMeasurementValue() > 180 || r.getMeasurementValue() < 90) {
                    triggerAlert(new Alert(String.valueOf(r.getPatientId()), "Critical Systolic: " + r.getMeasurementValue(), r.getTimestamp()));
                }
            } else if ("Diastolic".equalsIgnoreCase(r.getRecordType())) {
                diastolic.add(r);
                if (r.getMeasurementValue() > 120 || r.getMeasurementValue() < 60) {
                    triggerAlert(new Alert(String.valueOf(r.getPatientId()), "Critical Diastolic: " + r.getMeasurementValue(), r.getTimestamp()));
                }
            }
        }

        checkTrend(systolic, "Systolic", patient);
        checkTrend(diastolic, "Diastolic", patient);
    }

    /**
     * Evaluates a list of pressure readings to detect increasing or decreasing trends.
     * A trend alert is triggered if three consecutive values change by more than 10 mmHg
     * in the same direction.
     *
     * @param readings the sorted list of pressure readings (systolic or diastolic)
     * @param type the type of pressure ("Systolic" or "Diastolic")
     * @param patient the patient whose readings are being evaluated
     */
    private void checkTrend(List<PatientRecord> readings, String type, Patient patient) {
        readings.sort(Comparator.comparingLong(PatientRecord::getTimestamp));
        for (int i = 0; i <= readings.size() - 3; i++) {
            double v1 = readings.get(i).getMeasurementValue();
            double v2 = readings.get(i + 1).getMeasurementValue();
            double v3 = readings.get(i + 2).getMeasurementValue();
            long ts = readings.get(i + 2).getTimestamp();

            if ((v2 - v1 > 10) && (v3 - v2 > 10)) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), type + " Increasing Trend", ts));
            } else if ((v1 - v2 > 10) && (v2 - v3 > 10)) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), type + " Decreasing Trend", ts));
            }
        }
    }

    /**
     * Evaluates blood oxygen saturation levels for:
     * - Low saturation alerts (values < 92%)
     * - Rapid drop alerts (drop of 5% or more within 10 minutes)
     *
     * @param patient the patient whose oxygen saturation records are evaluated
     */
    public void checkBloodSaturationAlerts(Patient patient) {
        List<PatientRecord> records = patient.getRecords(0, Long.MAX_VALUE);
        List<PatientRecord> saturation = records.stream()
                .filter(r -> "BloodSaturation".equalsIgnoreCase(r.getRecordType()))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
                .collect(Collectors.toList());

        for (int i = 0; i < saturation.size(); i++) {
            PatientRecord r = saturation.get(i);
            if (r.getMeasurementValue() < 92) {
                triggerAlert(new Alert(String.valueOf(r.getPatientId()), "Low Oxygen Saturation", r.getTimestamp()));
            }

            // Check for 5% drop in the last 10 minutes (600_000 ms)
            for (int j = i + 1; j < saturation.size(); j++) {
                PatientRecord next = saturation.get(j);
                if (next.getTimestamp() - r.getTimestamp() > 600_000) break;

                double drop = r.getMeasurementValue() - next.getMeasurementValue();
                if (drop >= 5) {
                    triggerAlert(new Alert(String.valueOf(r.getPatientId()), "Rapid O2 Saturation Drop", next.getTimestamp()));
                    break;
                }
            }
        }
    }

    /**
     * Detects critical condition where the patient's systolic pressure is below 90 mmHg
     * and blood oxygen saturation is below 92% within a 5-minute window of each other.
     * This alert indicates a potential medical emergency.
     *
     * @param patient the patient whose data is being analyzed for combined indicators
     */
    public void checkHypotensiveHypoxemia(Patient patient) {
        List<PatientRecord> records = patient.getRecords(0, Long.MAX_VALUE);
        List<PatientRecord> systolic = new ArrayList<>();
        List<PatientRecord> saturation = new ArrayList<>();

        for (PatientRecord r : records) {
            if ("Systolic".equalsIgnoreCase(r.getRecordType())) {
                systolic.add(r);
            } else if ("BloodSaturation".equalsIgnoreCase(r.getRecordType())) {
                saturation.add(r);
            }
        }

        systolic.sort(Comparator.comparingLong(PatientRecord::getTimestamp));
        saturation.sort(Comparator.comparingLong(PatientRecord::getTimestamp));

        for (PatientRecord s : systolic) {
            if (s.getMeasurementValue() < 90) {
                for (PatientRecord o2 : saturation) {
                    if (Math.abs(s.getTimestamp() - o2.getTimestamp()) < 5 * 60 * 1000 // 5 minutes
                            && o2.getMeasurementValue() < 92) {
                        triggerAlert(new Alert(String.valueOf(s.getPatientId()), "Hypotensive Hypoxemia Alert", s.getTimestamp()));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Evaluates ECG signal values to detect abnormal peaks. Uses a sliding window average,
     * and triggers an alert if any reading exceeds 1.5 times the local window average.
     *
     * @param patient the patient whose ECG readings are analyzed
     */
    public void checkECGAlerts(Patient patient) {
        List<PatientRecord> records = patient.getRecords(0, Long.MAX_VALUE);
        List<PatientRecord> ecg = records.stream()
                .filter(r -> "ECG".equalsIgnoreCase(r.getRecordType()))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
                .collect(Collectors.toList());

        final int WINDOW_SIZE = 10;
        for (int i = 0; i <= ecg.size() - WINDOW_SIZE; i++) {
            List<PatientRecord> window = ecg.subList(i, i + WINDOW_SIZE);
            double avg = window.stream().mapToDouble(PatientRecord::getMeasurementValue).average().orElse(0);

            for (PatientRecord r : window) {
                if (r.getMeasurementValue() > avg * 1.5) {
                    triggerAlert(new Alert(String.valueOf(r.getPatientId()), "Abnormal ECG Peak", r.getTimestamp()));
                }
            }
        }
    }

    /**
     * Detects manually triggered alerts by checking for "ManualAlert" records in the patient's data.
     *
     * @param patient the patient whose records are scanned for manual alert events
     */
    public void checkManualAlerts(Patient patient) {
        List<PatientRecord> records = patient.getRecords(0, Long.MAX_VALUE);
        for (PatientRecord r : records) {
            if ("ManualAlert".equalsIgnoreCase(r.getRecordType())) {
                triggerAlert(new Alert(String.valueOf(r.getPatientId()), "Manual Alert Triggered", r.getTimestamp()));
            }
        }
    }



}
