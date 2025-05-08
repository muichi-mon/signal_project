package com.cardio_generator.outputs;

/**
 * Defines the strategy interface for outputting simulated data.
 * Implementations can output data to various destinations like console, file, etc.
 */
public interface OutputStrategy {
    void output(int patientId, long timestamp, String label, String data);
}
