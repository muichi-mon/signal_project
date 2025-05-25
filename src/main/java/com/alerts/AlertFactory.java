package com.alerts;

/**
 * Centralized AlertFactory with subclasses for different alert types.
 */
public abstract class AlertFactory {
    public abstract Alert createAlert(String patientId, String condition, long timestamp);

    // Factory for blood pressure alerts
    public static class BloodPressureAlertFactory extends AlertFactory {
        @Override
        public Alert createAlert(String patientId, String condition, long timestamp) {
            return new Alert(patientId, "Blood Pressure Alert - " + condition, timestamp);
        }
    }

    // Factory for blood oxygen saturation alerts
    public static class BloodOxygenAlertFactory extends AlertFactory {
        @Override
        public Alert createAlert(String patientId, String condition, long timestamp) {
            return new Alert(patientId, "Oxygen Saturation Alert - " + condition, timestamp);
        }
    }

    // Factory for combined hypotension and hypoxemia alerts
    public static class HypotensiveHypoxemiaAlertFactory extends AlertFactory {
        @Override
        public Alert createAlert(String patientId, String condition, long timestamp) {
            return new Alert(patientId, "Hypotensive Hypoxemia Alert - " + condition, timestamp);
        }
    }

    // Factory for ECG-related alerts
    public static class ECGAlertFactory extends AlertFactory {
        @Override
        public Alert createAlert(String patientId, String condition, long timestamp) {
            return new Alert(patientId, "ECG Alert - " + condition, timestamp);
        }
    }

    // Factory for manually triggered alerts
    public static class ManualAlertFactory extends AlertFactory {
        @Override
        public Alert createAlert(String patientId, String condition, long timestamp) {
            return new Alert(patientId, "Manual Alert - " + condition, timestamp);
        }
    }
}
